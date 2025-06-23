# Android LLM App with On-Device Models

This Android application demonstrates how to run two language models locally on Android devices:

- **M1**: A smaller instruction-following LLM for text processing
- **M2**: Gemma multimodal model for text and image processing

## Features

- Google Sign-In authentication
- Camera integration for image capture
- On-device TensorFlow Lite model inference
- Real-time text generation
- Multimodal input processing (text + image)
- Notification system for model completion
- Permission handling for camera and storage
- GPU acceleration support (when available)

## Prerequisites

- Android Studio (latest version)
- Android device with at least 8GB RAM (for Gemma) or Android emulator with ample resources
- Basic knowledge of Kotlin and Android development
- Hugging Face account (to access Gemma models)

## Setup Instructions

### 1. Clone and Open Project

1. Open Android Studio
2. Select "Open an existing Android Studio project"
3. Navigate to this project directory and open it

### 2. Configure Google Sign-In

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable Google Sign-In API
4. Create OAuth 2.0 credentials
5. Add your SHA-1 fingerprint to the credentials
6. Download the `google-services.json` file and place it in the `app/` directory

### 3. Add Model Files

#### For M1 (Instruction-Following Model):
1. Download a TensorFlow Lite model (e.g., GPT-2, TinyLLaMA)
2. Place the model file in `app/src/main/assets/` as `instruction_model.tflite`

#### For M2 (Gemma Multimodal):
1. Request access to Gemma on Hugging Face
2. Convert the model to TensorFlow Lite format
3. Place the model file in `app/src/main/assets/` as `gemma_model.tflite`

### 4. Build and Run

1. Sync project with Gradle files
2. Connect your Android device or start an emulator
3. Click "Run" in Android Studio

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/llmapp/
│   │   ├── MainActivity.kt              # Main UI and logic
│   │   ├── LLMApplication.kt            # Application class
│   │   ├── GoogleAuthHelper.kt          # Google Sign-In helper
│   │   ├── PermissionHelper.kt          # Runtime permissions
│   │   ├── NotificationHelper.kt        # Notification system
│   │   ├── ModelOptimizer.kt            # Model optimization
│   │   ├── Tokenizer.kt                 # Text tokenization
│   │   ├── ImageProcessor.kt            # Image preprocessing
│   │   ├── InstructionFollowingModel.kt # M1 model manager
│   │   └── GemmaMultimodalModel.kt      # M2 model manager
│   ├── res/
│   │   ├── layout/activity_main.xml     # Main UI layout
│   │   ├── values/strings.xml           # String resources
│   │   └── ...
│   └── AndroidManifest.xml              # App manifest
├── build.gradle                         # App-level build config
└── proguard-rules.pro                   # ProGuard rules
```

## Usage

1. **Login**: Use Google Sign-In to authenticate
2. **Text Input**: Enter prompts in the text field
3. **Image Capture**: Use the camera to capture images for multimodal input
4. **Run Models**:
   - Click "Run Instruction Model (M1)" for text-only processing
   - Click "Run Multimodal Model (M2)" for text + image processing
5. **View Results**: Model outputs appear in the respective text areas
6. **Notifications**: Get notified when models complete processing

## Model Integration

### Current Implementation
The app currently uses mock models for demonstration purposes. To integrate real models:

1. **Replace Mock Models**: Update the model loading code in `InstructionFollowingModel.kt` and `GemmaMultimodalModel.kt`
2. **Add Model Files**: Place your TensorFlow Lite models in the assets folder
3. **Update Tokenizers**: Implement proper tokenization for your specific models
4. **Optimize Performance**: Use the `ModelOptimizer` class to enable GPU acceleration

### Example Model Loading
```kotlin
private fun loadRealModel(): Interpreter {
    val modelFile = loadModelFile(context, "your_model.tflite")
    val options = modelOptimizer.getBestInterpreterOptions()
    return Interpreter(modelFile, options)
}
```

## Performance Optimization

- **GPU Acceleration**: Automatically enabled when available
- **NNAPI**: Used on Android P+ devices
- **Multi-threading**: CPU execution uses all available cores
- **Model Quantization**: Use quantized models for better performance
- **Memory Management**: Models are loaded once and reused

## Troubleshooting

### Common Issues

1. **Out of Memory**: Ensure device has sufficient RAM (8GB+ recommended)
2. **Model Loading Failed**: Check model file path and format
3. **Camera Not Working**: Grant camera permissions in app settings
4. **Google Sign-In Failed**: Verify OAuth credentials and SHA-1 fingerprint

### Debug Tips

- Check Logcat for detailed error messages
- Use Android Studio's Memory Profiler to monitor memory usage
- Test on different devices to ensure compatibility

## Dependencies

- **TensorFlow Lite**: 2.14.0 for on-device ML
- **CameraX**: 1.3.2 for camera functionality
- **Google Play Services**: 21.0.0 for authentication
- **Dexter**: 6.2.3 for permission handling
- **Coroutines**: 1.7.3 for asynchronous operations

## License

This project is for educational purposes. Please ensure you comply with the licenses of any models you integrate.

## Contributing

Feel free to submit issues and enhancement requests!

## Support

For questions or issues, please check the troubleshooting section or create an issue in the repository. 