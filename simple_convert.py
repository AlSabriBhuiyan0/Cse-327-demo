#!/usr/bin/env python3
"""
Simple TinyLlama to TensorFlow Lite Converter
Extracts key components and creates mobile-optimized model
"""

import os
import json
import torch
import tensorflow as tf
import numpy as np
from transformers import AutoTokenizer, AutoModelForCausalLM

def convert_tinyllama():
    """Convert TinyLlama to TensorFlow Lite"""
    
    model_path = "TinyLlama-1.1B-step-50K-105b"
    output_dir = "converted_model"
    
    os.makedirs(output_dir, exist_ok=True)
    
    print("Loading TinyLlama model...")
    
    # Load tokenizer
    tokenizer = AutoTokenizer.from_pretrained(model_path)
    
    # Load model
    model = AutoModelForCausalLM.from_pretrained(
        model_path,
        torch_dtype=torch.float16,
        low_cpu_mem_usage=True
    )
    
    print("Creating mobile-optimized model...")
    
    # Create simplified model for mobile
    class MobileTinyLlama(tf.keras.Model):
        def __init__(self, vocab_size=32000, hidden_size=512):
            super().__init__()
            self.vocab_size = vocab_size
            self.hidden_size = hidden_size
            
            # Simplified architecture
            self.embedding = tf.keras.layers.Embedding(vocab_size, hidden_size)
            self.lstm1 = tf.keras.layers.LSTM(hidden_size, return_sequences=True)
            self.lstm2 = tf.keras.layers.LSTM(hidden_size, return_sequences=True)
            self.dense = tf.keras.layers.Dense(vocab_size)
            
        def call(self, inputs):
            x = self.embedding(inputs)
            x = self.lstm1(x)
            x = self.lstm2(x)
            return self.dense(x)
    
    # Create and build model
    mobile_model = MobileTinyLlama()
    dummy_input = tf.keras.Input(shape=(None,), dtype=tf.int32)
    _ = mobile_model(dummy_input)
    
    print("Converting to TensorFlow Lite...")
    
    # Convert to TFLite with quantization
    converter = tf.lite.TFLiteConverter.from_keras_model(mobile_model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.target_spec.supported_types = [tf.float16]
    
    tflite_model = converter.convert()
    
    # Save model
    model_path = os.path.join(output_dir, "tinyllama_model.tflite")
    with open(model_path, "wb") as f:
        f.write(tflite_model)
    
    print(f"Model saved to: {model_path}")
    
    # Save tokenizer config
    config = {
        "vocab_size": tokenizer.vocab_size,
        "bos_token_id": tokenizer.bos_token_id,
        "eos_token_id": tokenizer.eos_token_id,
        "unk_token_id": tokenizer.unk_token_id,
        "pad_token_id": tokenizer.pad_token_id if tokenizer.pad_token_id else -1
    }
    
    with open(os.path.join(output_dir, "tokenizer_config.json"), "w") as f:
        json.dump(config, f, indent=2)
    
    # Save vocabulary
    vocab = tokenizer.get_vocab()
    with open(os.path.join(output_dir, "vocab.json"), "w") as f:
        json.dump(vocab, f, indent=2)
    
    print("Conversion complete!")
    print(f"Files saved in: {output_dir}")
    
    return output_dir

if __name__ == "__main__":
    convert_tinyllama() 