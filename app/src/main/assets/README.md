# Model Files Directory

Place your TensorFlow Lite model files in this directory:

## Required Files

1. **instruction_model.tflite** - For M1 (Instruction-following LLM)
   - Download a smaller model like GPT-2 or TinyLLaMA
   - Convert to TensorFlow Lite format
   - Recommended size: < 100MB

2. **gemma_model.tflite** - For M2 (Gemma Multimodal)
   - Request access to Gemma on Hugging Face
   - Convert to TensorFlow Lite format
   - Recommended size: < 2GB (quantized)

## Model Sources

- **Hugging Face**: https://huggingface.co/models
- **TensorFlow Hub**: https://tfhub.dev/
- **Model Zoo**: Various pre-trained models

## Conversion Tools

- **TensorFlow Lite Converter**: Convert TensorFlow models
- **ONNX to TFLite**: Convert ONNX models
- **Hugging Face Transformers**: Export models to TFLite

## Notes

- Use quantized models for better performance
- Ensure models are compatible with TensorFlow Lite
- Test models on target devices before deployment 