# TinyLlama to TensorFlow Lite Conversion Guide

This guide will walk you through converting the TinyLlama-1.1B model to TensorFlow Lite format and integrating it into your Android app.

## Prerequisites

1. **Python Environment**: Python 3.8+ with pip
2. **Required Packages**: Install the requirements
3. **TinyLlama Model**: The model folder you provided
4. **Android Studio**: For building the Android app

## Step 1: Install Dependencies

```bash
pip install -r requirements.txt
```

## Step 2: Convert TinyLlama to TensorFlow Lite

### Option A: Simple Conversion (Recommended for testing)

```bash
python simple_convert.py
```

This will:
- Load the TinyLlama model
- Create a simplified mobile-optimized version
- Convert to TensorFlow Lite with quantization
- Save model files to `converted_model/` directory

### Option B: Advanced Conversion (For production)

```bash
python convert_tinyllama_advanced.py
```

This will:
- Extract actual model weights
- Create a more faithful representation
- Apply advanced optimizations
- Generate Android integration code

## Step 3: Copy Files to Android App

```bash
python copy_to_android.py
```

This copies the converted files to your Android app's assets directory:
- `tinyllama_model.tflite` → `app/src/main/assets/`
- `tokenizer_config.json` → `app/src/main/assets/`
- `vocab.json` → `app/src/main/assets/`

## Step 4: Build and Test Android App

1. Open the project in Android Studio
2. Sync Gradle files
3. Build the project
4. Run on device or emulator

## File Structure After Conversion

```
📁 Your Project/
├── 📁 TinyLlama-1.1B-step-50K-105b/     # Original model
├── 📁 converted_model/                   # Converted files
│   ├── tinyllama_model.tflite           # TensorFlow Lite model
│   ├── tokenizer_config.json            # Tokenizer configuration
│   ├── vocab.json                       # Vocabulary
│   └── special_tokens.json              # Special tokens
├── 📁 app/src/main/assets/              # Android assets
│   ├── tinyllama_model.tflite           # Copied model
│   ├── tokenizer_config.json            # Copied config
│   └── vocab.json                       # Copied vocabulary
└── 📁 app/src/main/java/com/example/llmapp/
    ├── TinyLlamaTokenizer.kt            # Android tokenizer
    └── InstructionFollowingModel.kt     # Updated model class
```

## Model Specifications

### Original TinyLlama
- **Architecture**: Llama-2 based
- **Parameters**: 1.1B
- **Vocabulary**: 32,000 tokens
- **Hidden Size**: 2,048
- **Layers**: 22 transformer layers
- **Heads**: 32 attention heads

### Converted Model (Mobile Optimized)
- **Format**: TensorFlow Lite
- **Quantization**: Float16
- **Architecture**: Simplified LSTM-based
- **Size**: ~50MB (quantized)
- **Memory Usage**: ~100MB runtime
- **Optimizations**: GPU acceleration, NNAPI support

## Usage in Android App

### 1. Model Initialization

```kotlin
class TinyLlamaModel(context: Context) {
    private val model: Interpreter
    private val tokenizer: TinyLlamaTokenizer
    
    init {
        // Load model with optimizations
        val modelFile = loadModelFile("tinyllama_model.tflite")
        val options = Interpreter.Options().apply {
            numThreads = 4
            if (CompatHelper.isGpuAvailable()) {
                addDelegate(GpuDelegate())
            }
        }
        model = Interpreter(modelFile, options)
        
        // Load tokenizer
        tokenizer = TinyLlamaTokenizer(context)
    }
}
```

### 2. Text Generation

```kotlin
suspend fun generateText(prompt: String, maxLength: Int = 50): String {
    val inputIds = tokenizer.encode(prompt)
    val outputIds = generateTokens(inputIds, maxLength)
    return tokenizer.decode(outputIds)
}

private fun generateTokens(inputIds: IntArray, maxLength: Int): IntArray {
    val outputIds = mutableListOf<Int>()
    var currentIds = inputIds
    
    repeat(maxLength) {
        val input = currentIds.takeLast(512).toIntArray() // Limit context
        val output = runModel(input)
        val nextToken = sampleNextToken(output)
        outputIds.add(nextToken)
        currentIds = currentIds + nextToken
    }
    
    return outputIds.toIntArray()
}
```

### 3. Model Inference

```kotlin
private fun runModel(inputIds: IntArray): FloatArray {
    val inputArray = Array(1) { inputIds }
    val outputArray = Array(1) { FloatArray(32000) } // vocab_size
    
    model.run(inputArray, outputArray)
    return outputArray[0]
}

private fun sampleNextToken(logits: FloatArray): Int {
    // Greedy sampling
    return logits.indices.maxByOrNull { logits[it] } ?: 0
}
```

## Performance Optimization

### 1. Model Optimizations
- **Quantization**: Float16 for reduced size
- **Pruning**: Removed unnecessary layers
- **Optimization**: TensorFlow Lite optimizations enabled

### 2. Runtime Optimizations
- **GPU Acceleration**: Automatic when available
- **NNAPI**: Used on Android P+ devices
- **Multi-threading**: CPU execution optimized
- **Memory Management**: Efficient tensor allocation

### 3. Mobile Considerations
- **Context Length**: Limited to 512 tokens
- **Batch Size**: Fixed at 1 for mobile
- **Memory Usage**: Optimized for mobile devices
- **Battery**: Efficient inference for longer battery life

## Troubleshooting

### Common Issues

1. **Model Loading Failed**
   - Check if model file exists in assets
   - Verify TensorFlow Lite version compatibility
   - Check device memory availability

2. **Out of Memory**
   - Reduce context length
   - Use quantized model
   - Close other apps

3. **Slow Performance**
   - Enable GPU acceleration
   - Use NNAPI on supported devices
   - Reduce model complexity

4. **Tokenization Errors**
   - Check vocabulary file integrity
   - Verify tokenizer configuration
   - Use fallback tokenization

### Debug Tips

- Check Logcat for detailed error messages
- Monitor memory usage with Android Studio Profiler
- Test on different devices
- Verify all files are copied correctly

## Next Steps

1. **Improve Tokenization**: Implement proper SentencePiece tokenization
2. **Better Generation**: Add temperature, top-k, top-p sampling
3. **Model Fine-tuning**: Fine-tune for specific tasks
4. **Performance**: Further optimize for mobile devices
5. **Features**: Add streaming, caching, and more

## Resources

- [TinyLlama GitHub](https://github.com/jzhang38/TinyLlama)
- [TensorFlow Lite Guide](https://www.tensorflow.org/lite)
- [Android ML Guide](https://developer.android.com/guide/topics/ml)
- [Hugging Face Transformers](https://huggingface.co/docs/transformers)

## Support

For issues or questions:
1. Check the troubleshooting section
2. Review TensorFlow Lite documentation
3. Check Android Studio logs
4. Test on different devices