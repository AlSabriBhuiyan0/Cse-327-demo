package com.example.llmapp

import android.os.Build
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate

class ModelOptimizer(private val context: Context) {
    fun getBestInterpreterOptions(): Interpreter.Options {
        return Interpreter.Options().apply {
            when {
                CompatHelper.isGpuAvailable() -> {
                    addDelegate(GpuDelegate())
                    Log.d("ModelOptimizer", "Using GPU delegate")
                }
                CompatHelper.isNNApiAvailable() -> {
                    setUseNNAPI(true)
                    Log.d("ModelOptimizer", "Using NNAPI")
                }
                else -> {
                    numThreads = Runtime.getRuntime().availableProcessors()
                    Log.d("ModelOptimizer", "Using CPU with $numThreads threads")
                }
            }
        }
    }
}

object CompatHelper {
    fun isGpuAvailable(): Boolean {
        return try {
            Class.forName("org.tensorflow.lite.gpu.GpuDelegate")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun isNNApiAvailable(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }
} 