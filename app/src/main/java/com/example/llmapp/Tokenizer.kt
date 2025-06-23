package com.example.llmapp

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class Tokenizer(private val context: Context) {
    private val vocab = mutableMapOf<String, Int>()
    private val reverseVocab = mutableMapOf<Int, String>()
    
    init {
        loadVocabulary()
    }
    
    private fun loadVocabulary() {
        try {
            // For demo purposes, create a simple vocabulary
            // In a real implementation, you would load from a vocab file
            val basicTokens = listOf(
                "<pad>", "UNKNOWN", "<sos>", "<eos>",
                "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
                "of", "with", "by", "is", "are", "was", "were", "be", "been", "being",
                "have", "has", "had", "do", "does", "did", "will", "would", "could",
                "should", "may", "might", "can", "this", "that", "these", "those",
                "i", "you", "he", "she", "it", "we", "they", "me", "him", "her",
                "us", "them", "my", "your", "his", "her", "its", "our", "their"
            )
            
            basicTokens.forEachIndexed { index, token ->
                vocab[token] = index
                reverseVocab[index] = token
            }
        } catch (e: Exception) {
            // Fallback to basic tokenization
        }
    }
    
    fun encode(text: String): IntArray {
        // Simple word-based tokenization for demo
        return text.lowercase()
            .split(Regex("\\s+"))
            .map { token -> vocab[token] ?: vocab["UNKNOWN"] ?: 0 }
            .toIntArray()
    }
    
    fun decode(tokenIds: IntArray): String {
        return tokenIds
            .map { id -> reverseVocab[id] ?: "UNKNOWN" }
            .joinToString(" ")
    }
}