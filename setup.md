# Android LLM App Setup Guide

## Quick Start

### 1. Prerequisites
- Android Studio (latest version)
- Android device with 8GB+ RAM or emulator with 4GB+ RAM
- Google Cloud Console account
- Hugging Face account (for Gemma access)

### 2. Open Project in Android Studio
1. Launch Android Studio
2. Select "Open an existing Android Studio project"
3. Navigate to this project folder and open it
4. Wait for Gradle sync to complete

### 3. Configure Google Sign-In

#### Step 1: Create Google Cloud Project
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable the Google Sign-In API:
   - Go to "APIs & Services" > "Library"
   - Search for "Google Sign-In API"
   - Click "Enable"

#### Step 2: Create OAuth Credentials
1. Go to "APIs & Services" > "Credentials"
2. Click "Create Credentials" > "OAuth 2.0 Client IDs"
3. Select "Android" as application type
4. Add your package name: `com.example.llmapp`
5. Get your SHA-1 fingerprint:
   ```bash
   # For debug keystore (default)
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   
   # For release keystore (if you have one)
   keytool -list -v -keystore your-release-key.keystore -alias your-key-alias
   ```
6. Add the SHA-1 fingerprint to the credentials
7. Download the `google-services.json` file

#### Step 3: Add google-services.json
1. Place the downloaded `google-services.json` file in the `app/` directory
2. Sync project with Gradle files

### 4. Add Model Files

#### For M1 (Instruction-Following Model)
1. Download a TensorFlow Lite model:
   - [Hugging Face Models](https://huggingface.co/models?search=tflite)
   - [TensorFlow Hub](https://tfhub.dev/)
   - Recommended: GPT-2 or TinyLLaMA (quantized)

2. Convert to TensorFlow Lite format (if needed):
   ```python
   import tensorflow as tf
   
   # Load your model
   model = tf.keras.models.load_model('your_model.h5')
   
   # Convert to TFLite
   converter = tf.lite.TFLiteConverter.from_keras_model(model)
   converter.optimizations = [tf.lite.Optimize.DEFAULT]
   tflite_model = converter.convert()
   
   # Save
   with open('instruction_model.tflite', 'wb') as f:
       f.write(tflite_model)
   ```

3. Place the model file in `app/src/main/assets/instruction_model.tflite`

#### For M2 (Gemma Multimodal)
1. Request access to Gemma on Hugging Face:
   - Go to [Gemma Models](https://huggingface.co/google/gemma-2b)
   - Click "Request access"
   - Accept the terms and conditions

2. Download and convert the model:
   ```python
   from transformers import AutoTokenizer, AutoModelForCausalLM
   import torch
   
   # Load model and tokenizer
   model_name = "google/gemma-2b"
   tokenizer = AutoTokenizer.from_pretrained(model_name)
   model = AutoModelForCausalLM.from_pretrained(model_name)
   
   # Convert to TensorFlow Lite (simplified example)
   # You may need to use ONNX as intermediate format
   ```

3. Place the model file in `app/src/main/assets/gemma_model.tflite`

### 5. Update Model Loading Code

#### For Real Models (Optional)
If you want to use real models instead of mock ones:

1. Update `InstructionFollowingModel.kt`:
   ```kotlin
   private fun loadRealModel(): Interpreter {
       val modelFile = loadModelFile(context, "instruction_model.tflite")
       val options = modelOptimizer.getBestInterpreterOptions()
       return Interpreter(modelFile, options)
   }
   ```

2. Update `GemmaMultimodalModel.kt`:
   ```kotlin
   private fun loadRealModel(): Interpreter {
       val modelFile = loadModelFile(context, "gemma_model.tflite")
       val options = modelOptimizer.getBestInterpreterOptions()
       return Interpreter(modelFile, options)
   }
   ```

### 6. Build and Run

1. Connect your Android device or start an emulator
2. Click "Run" in Android Studio
3. Grant permissions when prompted
4. Test the app functionality

## Testing the App

### Basic Functionality
1. **Login**: Use Google Sign-In
2. **Text Input**: Enter a prompt like "Hello, how are you?"
3. **M1 Model**: Click "Run Instruction Model (M1)"
4. **Camera**: Click "Open Camera" to capture an image
5. **M2 Model**: Click "Run Multimodal Model (M2)"

### Expected Behavior
- Mock responses will be generated for demonstration
- Camera should open and capture images
- Notifications should appear when models complete
- UI should update based on login status

## Troubleshooting

### Common Issues

1. **Gradle Sync Failed**
   - Check internet connection
   - Update Android Studio
   - Clean and rebuild project

2. **Google Sign-In Not Working**
   - Verify `google-services.json` is in correct location
   - Check SHA-1 fingerprint matches
   - Ensure Google Sign-In API is enabled

3. **Camera Not Working**
   - Grant camera permissions in app settings
   - Check device has camera
   - Test on physical device (emulator camera may not work)

4. **Out of Memory**
   - Use device with more RAM
   - Reduce model size
   - Use quantized models

5. **Model Loading Failed**
   - Check model file exists in assets
   - Verify model format is TensorFlow Lite
   - Check model compatibility

### Debug Tips
- Check Logcat for detailed error messages
- Use Android Studio's Memory Profiler
- Test on different devices
- Verify all permissions are granted

## Next Steps

1. **Replace Mock Models**: Integrate real TensorFlow Lite models
2. **Optimize Performance**: Use GPU acceleration and model quantization
3. **Add Features**: Implement model caching, progress indicators
4. **Customize UI**: Modify the layout and styling
5. **Add More Models**: Integrate additional language models

## Support

For issues or questions:
1. Check the troubleshooting section
2. Review the README.md file
3. Check Android Studio logs
4. Test on different devices 