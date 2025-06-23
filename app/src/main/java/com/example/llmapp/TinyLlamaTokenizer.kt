package com.example.llmapp

import android.content.Context
import org.json.JSONObject
import java.io.InputStream

class TinyLlamaTokenizer(private val context: Context) {
    private val vocab = mutableMapOf<String, Int>()
    private val reverseVocab = mutableMapOf<Int, String>()
    private val config: JSONObject
    
    init {
        loadVocabulary()
        config = loadConfig()
    }
    
    private fun loadVocabulary() {
        try {
            val inputStream: InputStream = context.assets.open("vocab.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = jsonObject.getInt(key)
                vocab[key] = value
                reverseVocab[value] = key
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to basic vocabulary
            loadBasicVocabulary()
        }
    }
    
    private fun loadBasicVocabulary() {
        val basicTokens = listOf(
            "<pad>", "<unk>", "<s>", "</s>",
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
    }
    
    private fun loadConfig(): JSONObject {
        return try {
            val inputStream: InputStream = context.assets.open("tokenizer_config.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            JSONObject(jsonString)
        } catch (e: Exception) {
            JSONObject().apply {
                put("unk_token_id", 1)
                put("bos_token_id", 2)
                put("eos_token_id", 3)
                put("pad_token_id", 0)
            }
        }
    }
    
    fun encode(text: String): IntArray {
        // Simple word-based tokenization
        // In production, you would use proper tokenization
        return text.lowercase()
            .split(Regex("\\s+"))
            .map { token -> vocab[token] ?: config.getInt("unk_token_id") }
            .toIntArray()
    }
    
    fun decode(tokenIds: IntArray): String {
        return tokenIds
            .map { id -> reverseVocab[id] ?: "<unk>" }
            .joinToString(" ")
    }
    
    fun getVocabSize(): Int {
        return vocab.size
    }
    
    fun getUnkTokenId(): Int {
        return config.getInt("unk_token_id")
    }
    
    fun getBosTokenId(): Int {
        return config.getInt("bos_token_id")
    }
    
    fun getEosTokenId(): Int {
        return config.getInt("eos_token_id")
    }
} 