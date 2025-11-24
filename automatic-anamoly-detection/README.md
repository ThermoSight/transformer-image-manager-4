---
title: automatic-anamoly-detection
sdk: gradio
pinned: false
---

# Automatic Anomaly Detection (PatchCore)

Gradio UI around the PatchCore inference pipeline. Upload an image to get boxed anomaly visualization and JSON detections. The same `app` object is the Gradio Blocks exported by the Space (no extra servers).

## Local setup (venv)
- `python -m venv .venv && .venv\Scripts\activate` on Windows or `python -m venv .venv && source .venv/bin/activate` on Linux/WSL.
- `pip install --upgrade pip setuptools wheel`
- `pip install -r requirements.txt`
- Run locally: `python app.py` then open the printed Gradio URL (or set `PORT=7860 python app.py`).

## Deploy to Hugging Face Space
1. `pip install huggingface_hub hf_transfer` (once) and `huggingface-cli login`.
2. `git clone https://huggingface.co/spaces/Lasidu/automatic-anamoly-detection` (or add a HF remote to this repo).
3. Copy this project into that clone (including `Model_Inference` and `app.py`).
4. Choose how to provide weights:
   - Track `Model_Inference/model_weights/model.ckpt` with Git LFS (already configured via `.gitattributes`) and push it, **or**
   - Keep weights out of git and add a Space secret `CKPT_URL` that points to your checkpoint; optionally set `CKPT_PATH` if you want a different on-disk location.
5. Commit and push to the Space: `git add . && git commit -m "Sync app + Gradio UI"` then `git push`.

## Gradio UI
- Upload an image, tweak sensitivity (0.1–2.0), optionally paste feedback JSON or provide a temporary `ckpt_url`.
- Outputs: predicted label, boxed image preview, and detection JSON rendered inline.
