"""
Helper to call the FastAPI endpoint locally or on the Hugging Face Space.

Example (remote):
  python scripts/call_hf_space.py --image Model_Inference/test_image/test01.jpg \
    --url https://lasidu-automatic-anamoly-detection.hf.space/infer \
    --save-dir outputs_remote

Example (local):
  python scripts/call_hf_space.py --image Model_Inference/test_image/test01.jpg \
    --url http://localhost:7860/infer --save-dir outputs_local
"""

import argparse
import base64
import json
import os
from pathlib import Path
from typing import Optional

import requests


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Call the PatchCore FastAPI endpoint.")
    parser.add_argument(
        "--image",
        required=True,
        help="Path to the image to send.",
    )
    parser.add_argument(
        "--url",
        default=os.environ.get(
            "HF_SPACE_URL", "https://lasidu-automatic-anamoly-detection.hf.space/infer"
        ),
        help="Endpoint URL (Space or local FastAPI).",
    )
    parser.add_argument(
        "--sensitivity",
        type=float,
        default=1.0,
        help="Detection sensitivity (0.1 - 2.0).",
    )
    parser.add_argument(
        "--feedback",
        type=str,
        default="",
        help="Path to a JSON feedback file to include (optional).",
    )
    parser.add_argument(
        "--ckpt-url",
        type=str,
        default=None,
        help="Optional checkpoint URL if the Space should download weights.",
    )
    parser.add_argument(
        "--save-dir",
        type=str,
        default="",
        help="Directory to save boxed image + JSON locally.",
    )
    return parser.parse_args()


def load_feedback(feedback_path: str) -> str:
    if not feedback_path:
        return ""
    path = Path(feedback_path)
    if not path.exists():
        raise FileNotFoundError(f"Feedback file not found: {feedback_path}")
    return path.read_text(encoding="utf-8")


def save_outputs(
    result: dict, image_path: Path, save_dir: Optional[Path]
) -> None:
    if not save_dir:
        return

    save_dir.mkdir(parents=True, exist_ok=True)
    stem = image_path.stem

    boxed_bytes = base64.b64decode(result["boxed_image_base64"])
    ext = result.get("boxed_image_ext") or ".png"
    boxed_path = save_dir / f"{stem}_boxed{ext}"
    boxed_path.write_bytes(boxed_bytes)

    json_path = save_dir / f"{stem}.json"
    json_path.write_text(json.dumps(result.get("json") or {}, indent=2), encoding="utf-8")

    print(f"[saved] {boxed_path}")
    print(f"[saved] {json_path}")


def main() -> None:
    args = parse_args()
    image_path = Path(args.image)
    if not image_path.exists():
        raise FileNotFoundError(f"Image not found: {image_path}")

    feedback_text = load_feedback(args.feedback)
    with image_path.open("rb") as fh:
        files = {"file": (image_path.name, fh, "application/octet-stream")}
        data = {
            "sensitivity": str(args.sensitivity),
            "feedback": feedback_text,
        }
        if args.ckpt_url:
            data["ckpt_url"] = args.ckpt_url

        print(f"[call] POST {args.url}")
        resp = requests.post(args.url, files=files, data=data, timeout=180)
        resp.raise_for_status()
        result = resp.json()

    print(f"[label] {result.get('label')}")
    print(f"[feedback_applied] {result.get('feedback_applied')}")
    save_outputs(result, image_path, Path(args.save_dir) if args.save_dir else None)


if __name__ == "__main__":
    main()
