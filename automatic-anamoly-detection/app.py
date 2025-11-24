"""
FastAPI + Gradio wrapper for PatchCore inference.
The Space exposes both an HTTP API and a simple web UI:
- POST /infer for programmatic use
- /ui for the Gradio upload -> boxed image experience
"""

import base64
import io
import json
import os
import shutil
import sys
import tempfile
from pathlib import Path
from threading import Lock
from typing import Optional

import gradio as gr
import numpy as np
import requests
import torch
from PIL import Image

# Ensure we can import inference_core_local from Model_Inference/
BASE_DIR = Path(__file__).parent.resolve()
MODEL_DIR = BASE_DIR / "Model_Inference"
sys.path.append(str(MODEL_DIR))
import inference_core_local as infer  # noqa: E402

CONFIG_PATH = str(MODEL_DIR / "config" / "patchcore_transformers.yaml")
DEFAULT_CKPT_PATH = MODEL_DIR / "model_weights" / "model.ckpt"
DEFAULT_INFER_SIZE = getattr(infer, "DEFAULT_INFER_SIZE", 256)

DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")

_model_lock = Lock()
_model = None
_loaded_ckpt: Optional[str] = None


def _clamp_sensitivity(raw_value: float) -> float:
    try:
        return max(0.1, min(2.0, float(raw_value)))
    except Exception:
        return 1.0


def _download_checkpoint(url: str, target_path: Path) -> Path:
    target_path.parent.mkdir(parents=True, exist_ok=True)
    with requests.get(url, stream=True, timeout=60) as resp:
        resp.raise_for_status()
        with open(target_path, "wb") as fh:
            for chunk in resp.iter_content(chunk_size=8192):
                if chunk:
                    fh.write(chunk)
    return target_path


def _ensure_checkpoint(override_url: Optional[str] = None) -> Path:
    """
    Ensure a .ckpt exists locally. If missing, try CKPT_URL env or override_url.
    """
    target = Path(os.environ.get("CKPT_PATH", DEFAULT_CKPT_PATH))
    if target.exists():
        return target

    ckpt_url = override_url or os.environ.get("CKPT_URL")
    if not ckpt_url:
        raise RuntimeError(
            "Model checkpoint not found. Provide CKPT_URL env or pass ckpt_url in the request."
        )

    return _download_checkpoint(ckpt_url, target)


def _load_model(ckpt_url: Optional[str] = None):
    global _model, _loaded_ckpt
    with _model_lock:
        ckpt_path = _ensure_checkpoint(ckpt_url)
        if _model is None or str(ckpt_path) != _loaded_ckpt:
            _model, _cfg = infer.load_model(CONFIG_PATH, str(ckpt_path), DEVICE)
            _loaded_ckpt = str(ckpt_path)
    return _model


def _parse_feedback(feedback_json: str):
    if not feedback_json:
        return {}
    try:
        return json.loads(feedback_json)
    except Exception as exc:  # pragma: no cover - defensive
        return {"error": f"Failed to parse feedback: {exc}"}


def _run_inference_bytes(
    file_bytes: bytes,
    filename: str,
    sensitivity: float,
    feedback_json: str,
    ckpt_url: Optional[str] = None,
):
    """
    Shared inference routine used by both the FastAPI endpoint and the Gradio UI.
    """
    sensitivity = _clamp_sensitivity(sensitivity)
    try:
        model = _load_model(ckpt_url)
    except Exception as exc:
        raise RuntimeError(f"Model load failed: {exc}") from exc

    tmp_dir = Path(tempfile.mkdtemp(prefix="patchcore_api_"))
    try:
        input_name = filename or "input.png"
        input_path = tmp_dir / input_name
        input_path.write_bytes(file_bytes)

        out_base = tmp_dir / "outputs"
        out_boxed = out_base / "boxed"
        out_mask = out_base / "masks"
        out_filtered = out_base / "filtered"
        for p in (out_boxed, out_mask, out_filtered):
            p.mkdir(parents=True, exist_ok=True)

        feedback_obj = _parse_feedback(feedback_json)
        with _model_lock:
            result = infer.run_pipeline_for_image(
                model,
                DEVICE,
                str(input_path),
                out_boxed_dir=str(out_boxed),
                out_mask_dir=str(out_mask),
                out_filtered_dir=str(out_filtered),
                infer_size=DEFAULT_INFER_SIZE,
                sensitivity=sensitivity,
                feedback=feedback_obj,
            )

        boxed_path = Path(result["boxed_path"])
        json_path = Path(result["json_path"])
        boxed_bytes = boxed_path.read_bytes()
        json_text = json_path.read_text(encoding="utf-8")

        return {
            "label": result.get("label"),
            "boxed_bytes": boxed_bytes,
            "boxed_image_ext": boxed_path.suffix or ".png",
            "json": json.loads(json_text),
            "json_text": json_text,
            "feedback_applied": bool(
                (result.get("feedback") or {}).get("applied", False)
            ),
        }
    finally:
        shutil.rmtree(tmp_dir, ignore_errors=True)


def _image_to_bytes(image) -> bytes:
    """
    Normalize a Gradio image input (numpy array or PIL Image) into PNG bytes.
    """
    if image is None:
        raise ValueError("No image provided.")

    if isinstance(image, np.ndarray):
        image = Image.fromarray(image.astype("uint8"))
    elif not isinstance(image, Image.Image):
        raise ValueError("Unsupported image type.")

    buf = io.BytesIO()
    image.save(buf, format="PNG")
    buf.seek(0)
    return buf.read()


def _gradio_predict(image, sensitivity=1.0, feedback_text="", ckpt_url=""):
    if image is None:
        return "Please upload an image.", None, {"error": "No image provided"}, False

    try:
        inference = _run_inference_bytes(
            _image_to_bytes(image),
            "upload.png",
            sensitivity,
            feedback_text,
            ckpt_url or None,
        )
        boxed_img = Image.open(io.BytesIO(inference["boxed_bytes"])).convert("RGB")
        return (
            inference.get("label"),
            boxed_img,
            inference.get("json"),
            bool(inference.get("feedback_applied")),
        )
    except Exception as exc:  # pragma: no cover - UI-friendly error path
        return f"Error: {exc}", None, {"error": str(exc)}, False


def _build_gradio() -> gr.Blocks:
    example_image = str(MODEL_DIR / "test_image" / "test01.jpg")

    with gr.Blocks(title="PatchCore Anomaly Detection") as demo:
        gr.Markdown(
            "## PatchCore Anomaly Detection\n"
            "Upload an image to get the boxed anomaly visualization and JSON detections. "
            "You can also tweak sensitivity or provide feedback JSON from your backend."
        )
        with gr.Row():
            with gr.Column():
                image_input = gr.Image(type="numpy", label="Upload image")
                sensitivity = gr.Slider(
                    minimum=0.1,
                    maximum=2.0,
                    step=0.05,
                    value=1.0,
                    label="Detection sensitivity",
                )
                feedback_box = gr.Textbox(
                    label="Feedback JSON (optional)",
                    placeholder='{"label_adjustments": {...}}',
                    lines=4,
                )
                ckpt_box = gr.Textbox(
                    label="Checkpoint URL override (optional)",
                    placeholder="https://.../model.ckpt",
                )
                run_btn = gr.Button("Run inference", variant="primary")
                gr.Examples(
                    examples=[[example_image, 1.0, "", ""]],
                    inputs=[image_input, sensitivity, feedback_box, ckpt_box],
                    label="Sample image",
                )

            with gr.Column():
                label_out = gr.Textbox(label="Predicted label", interactive=False)
                boxed_out = gr.Image(type="pil", label="Boxed output")
                json_out = gr.JSON(label="Detection JSON")
                feedback_applied = gr.Checkbox(
                    label="Feedback applied?", value=False, interactive=False
                )

        run_btn.click(
            fn=_gradio_predict,
            inputs=[image_input, sensitivity, feedback_box, ckpt_box],
            outputs=[label_out, boxed_out, json_out, feedback_applied],
            show_progress=True,
        )

    return demo


app = _build_gradio()


if __name__ == "__main__":  # pragma: no cover - manual run
    app.launch(server_name="0.0.0.0", server_port=int(os.environ.get("PORT", 7860)))
