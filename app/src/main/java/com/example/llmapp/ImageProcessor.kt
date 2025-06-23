package com.example.llmapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import kotlin.math.min

class ImageProcessor(private val context: Context) {
    companion object {
        const val INPUT_SIZE = 224
    }
    
    fun process(bitmap: Bitmap): FloatArray {
        // Resize image to model input size
        val resizedBitmap = resizeBitmap(bitmap, INPUT_SIZE, INPUT_SIZE)
        
        // Convert to float array and normalize
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        resizedBitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        
        val floatArray = FloatArray(INPUT_SIZE * INPUT_SIZE * 3)
        var pixelIndex = 0
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            floatArray[pixelIndex++] = ((pixel shr 16 and 0xFF) - 127.5f) / 127.5f  // Red
            floatArray[pixelIndex++] = ((pixel shr 8 and 0xFF) - 127.5f) / 127.5f   // Green
            floatArray[pixelIndex++] = ((pixel and 0xFF) - 127.5f) / 127.5f         // Blue
        }
        
        return floatArray
    }
    
    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val scaleWidth = width.toFloat() / bitmap.width
        val scaleHeight = height.toFloat() / bitmap.height
        val scale = min(scaleWidth, scaleHeight)
        
        val matrix = Matrix().apply {
            postScale(scale, scale)
        }
        
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
} 