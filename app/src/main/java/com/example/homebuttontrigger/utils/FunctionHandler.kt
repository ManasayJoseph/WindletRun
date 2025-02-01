package com.example.homebuttontrigger.utils

import android.util.Log
import com.example.homebuttontrigger.network.GeminiClient
import com.example.homebuttontrigger.network.GeminiRequest
import com.example.homebuttontrigger.network.Content
import com.example.homebuttontrigger.network.Part
import com.example.homebuttontrigger.network.Tool
import com.example.homebuttontrigger.network.GoogleSearch

object FunctionHandler {
    private const val API_KEY = "AIzaSyB3ndlmFIak9UlraZbcehwXTjcFZAMPv8Q"
    suspend fun handleQuery(query: String): Pair<String, String> {
        val category = classifyQuery(query)
        Log.w("FunctionHandler","category: $category")
        return when (category) {
            "function_calling" -> Pair(handleFunctionCall(query), handleFunctionCall(query))
            else -> handleGoogleSearch(query)
        }
    }

    private suspend fun classifyQuery(query: String): String {
        val classificationPrompt = """
            Classify the following question into one of these categories:
            - "google_search": For questions requiring real-time or factual answers.
            - "function_calling": For device-specific actions like calling, opening apps, etc.
            
            Question: $query
            Category:
        """.trimIndent()
        Log.w("FunctionHandler", "classifyQuery: $classificationPrompt")
        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = classificationPrompt))))
        )

        val response = GeminiClient.api.generateContent(API_KEY, request)
        Log.w("FunctionHandler", "classifyQuery: $response")
        Log.w("FunctionHandler",response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?.lowercase()?.trim() ?: "google_search")
        return response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?.lowercase()?.trim() ?: "google_search"
    }

    private fun handleFunctionCall(query: String): String {
        return when {
            query.contains("call") -> "Calling ${query.replace("call", "").trim()}..."
            query.contains("open") -> "Opening ${query.replace("open", "").trim()}..."
            query.contains("selfie") -> "SELFIE"
            else -> "Error: Function not found"
        }
    }

    private suspend fun handleGoogleSearch(query: String): Pair<String, String> {
        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = query)))),
            tools = listOf(Tool(googleSearch = GoogleSearch()))
        )

        val response = GeminiClient.api.generateContent(API_KEY, request)
        return if (response.isSuccessful) {
            val answer = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Error: Empty response"
            Pair(answer.take(40), answer)
        } else {
            Pair("Error", "API Error: ${response.errorBody()?.string()}")
        }
    }
}