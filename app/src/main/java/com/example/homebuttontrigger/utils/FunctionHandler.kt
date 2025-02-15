package com.example.homebuttontrigger.utils

import android.util.Log
import com.example.homebuttontrigger.network.ActionProperties
import com.example.homebuttontrigger.network.ActionSchema
import com.example.homebuttontrigger.network.ArgumentSchema
import com.example.homebuttontrigger.network.GeminiClient
import com.example.homebuttontrigger.network.GeminiRequest
import com.example.homebuttontrigger.network.Content
import com.example.homebuttontrigger.network.GenerationConfig
import com.example.homebuttontrigger.network.Part
import com.example.homebuttontrigger.network.ResponseProperties
import com.example.homebuttontrigger.network.ResponseSchema
import com.example.homebuttontrigger.network.SchemaArray
import com.example.homebuttontrigger.network.SchemaProperty
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

//import com.example.homebuttontrigger.network.Tool
//import com.example.homebuttontrigger.network.GoogleSearch

object FunctionHandler {
    private const val API_KEY = "AIzaSyB3ndlmFIak9UlraZbcehwXTjcFZAMPv8Q"

    //    suspend fun handleQuery(query: String): Pair<String, String> {
//        val category = classifyQuery(query)
//        Log.w("FunctionHandler","category: $category")
//        return when (category) {
//            "function_calling" -> Pair(handleFunctionCall(query), handleFunctionCall(query))
//            else -> handleGoogleSearch(query)
//        }
//    }
    suspend fun handleQuery(query: String): Pair<String, String> {
        var result = singleIntent(query)

        if (result != null) {
            Log.i("handleQuery", "Processed command: ${result.first} with value: ${result.second}")
            // Do something if the command is valid
            return result

        } else {
            Log.i("handleQuery", "Unknown command received")
            result = handleElse(query)
            return result ?: Pair("Error", "Unknown command")
        }
    }



    private fun singleIntent(query: String): Pair<String, String>? {
        val onDevice = OnDevice()
        val words = query.split(" ")

        when (words[0].lowercase()) {
            "call" -> {
                if (words.size == 2) {
                    onDevice.call(words[1])
                    return Pair("call", words[1])
                } else {
                    Log.i("InvalidFormat", "Invalid call format. Use: call <name>")
                }
            }

            "open" -> {
                if (words.size == 2) {
                    onDevice.openApp(words[1])
                    return Pair("open", words[1])
                } else {
                    Log.i("InvalidFormat", "Invalid open format. Use: open <app_name>")
                }
            }

            "send" -> {
                if (words.size >= 3) {
                    val message = words.subList(1, words.size - 1).joinToString(" ")
                    val name = words.last()
                    onDevice.send(name, message)
                    return Pair("send", "$message to $name")
                } else {
                    Log.i("InvalidFormat", "Invalid send format. Use: send <message> <name>")
                }
            }

            else -> {
                Log.i("SingleIntent", "Unknown command")
                return null  // Return null for unknown command
            }
        }
        return null
    }

    private suspend fun handleElse(query: String): Pair<String, String> {
        val classificationPrompt = """
            Categorize the "$query". Determine if it requires searching the internet, performing device actions, or both.
            
            - If a search is required, return it as "SearchQuery".
            - If one or more actions (function calls) are required, return them in "actions".
            - If an action (like sending a message) needs a search result as an argument, use "{{SearchResult}}" instead of the full query text.
            - If there are no actions required, omit "actions" from the response.
            - If no search is required, omit "SearchQuery" from the response.
            
            Available functions:
            - Call(name)
            - Open(application)
            - Send(Message, Medium=SMS, Contact)
            - Timer(time:seconds)
            
            Ensure:
            - Extract search-related parts separately under "SearchQuery".
            - Replace search-dependent arguments with "{{SearchResult}}".
        """.trimIndent()
        Log.w("FunctionHandler", "classifyQuery: $classificationPrompt")
        val request = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = classificationPrompt)))
            ),
            generationConfig = GenerationConfig(
                responseSchema = ResponseSchema(
                    properties = ResponseProperties(
                        SearchQuery = SchemaProperty(type = "string"),
                        actions = SchemaArray(
                            type = "array",
                            items = ActionSchema(
                                type = "object",
                                properties = ActionProperties(
                                    Function = SchemaProperty(type = "string"),
                                    Arguments = ArgumentSchema(
                                        type = "array",
                                        items = SchemaProperty(type = "string")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
        Log.w("OnDevice", "classifyQuery: $request")
        val response = GeminiClient.api.generateContent(API_KEY, request)
        Log.w("OnDevice", " categorization ${response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text}")

        return Pair("hello",query)
    }

//    private suspend fun classifyQuery(query: String): String {
//        val classificationPrompt = """
//            Classify the following question into one of these categories:
//            - "google_search": For questions requiring real-time or factual answers.
//            - "function_calling": For device-specific actions like calling, opening apps, etc.
//
//            Question: $query
//            Category:
//        """.trimIndent()
//        Log.w("FunctionHandler", "classifyQuery: $classificationPrompt")
//        val request = GeminiRequest(
//            contents = listOf(Content(parts = listOf(Part(text = classificationPrompt))))
//        )
//
//        val response = GeminiClient.api.generateContent(API_KEY, request)
//        Log.w("FunctionHandler", "classifyQuery: $response")
//        Log.w("FunctionHandler",response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
//            ?.lowercase()?.trim() ?: "google_search")
//        return response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
//            ?.lowercase()?.trim() ?: "google_search"
//    }

//    private fun handleFunctionCall(query: String): String {
//        return when {
//            query.contains("call") -> "Calling ${query.replace("call", "").trim()}..."
//            query.contains("open") -> "Opening ${query.replace("open", "").trim()}..."
//            query.contains("selfie") -> "SELFIE"
//            else -> "Error: Function not found"
//        }
//    }

//    private suspend fun handleGoogleSearch(query: String): Pair<String, String> {
//        val request = GeminiRequest(
//            contents = listOf(Content(parts = listOf(Part(text = query)))),
//            tools = listOf(Tool(googleSearch = GoogleSearch()))
//        )
//
//        val response = GeminiClient.api.generateContent(API_KEY, request)
//        return if (response.isSuccessful) {
//            val answer = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
//                ?: "Error: Empty response"
//            Pair(answer.take(40), answer)
//        } else {
//            Pair("Error", "API Error: ${response.errorBody()?.string()}")
//        }
//    }
}