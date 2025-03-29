//package com.example.homebuttontrigger.network
//
//import retrofit2.Response
//import retrofit2.http.Body
//import retrofit2.http.Header
//import retrofit2.http.POST
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//object GeminiClient {
//    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
//
//    val api: GeminiApiService by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(GeminiApiService::class.java)
//    }
//}
//
//interface GeminiApiService {
//    @POST("v1beta/models/gemini-2.0-flash-exp:generateContent")
//    suspend fun generateContent(
//        @Header("x-goog-api-key") apiKey: String,
//        @Body request: GeminiRequest
//    ): Response<GeminiResponse>
//}
////
////// Updated Request Model
////data class GeminiRequest(
////    val contents: List<Content>,
////    val tools: List<Tool> = listOf(Tool(googleSearch = GoogleSearch()))
////)
////
////data class Content(val parts: List<Part>)
////data class Part(val text: String)
////
////// GoogleSearch should not be a data class
//class GoogleSearch
//data class Tool(val googleSearch: GoogleSearch)
////
////// Response Model
//data class GeminiResponse(val candidates: List<Candidate>)
//data class Candidate(val content: Content)
//data class GeminiRequest(
//    val contents: List<Content>,
//    val generationConfig: GenerationConfig
//)
//
//data class Content(
//    val role: String = "user",
//    val parts: List<Part>
//)
//
//data class Part(
//    val text: String
//)
//
//data class GenerationConfig(
//    val temperature: Float = 1.0f,
//    val topK: Int = 40,
//    val topP: Float = 0.95f,
//    val maxOutputTokens: Int = 8192,
//    val responseMimeType: String = "application/json",
//    val responseSchema: ResponseSchema
//)
//
//data class ResponseSchema(
//    val type: String = "object",
//    val properties: ResponseProperties
//)
//
//data class ResponseProperties(
//    val SearchQuery: SchemaProperty,
//    val actions: SchemaArray
//)
//
//data class SchemaProperty(
//    val type: String = "string"
//)
//
//data class SchemaArray(
//    val type: String = "array",
//    val items: ActionSchema
//)
//
//data class ActionSchema(
//    val type: String = "object",
//    val properties: ActionProperties,
//    val required: List<String> = listOf("Function", "Arguments")
//)
//
//data class ActionProperties(
//    val Function: SchemaProperty,
//    val Arguments: ArgumentSchema
//)
//
//data class ArgumentSchema(
//    val type: String = "array",
//    val items: SchemaProperty
//)
package com.example.homebuttontrigger.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    val api: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
}

interface GeminiApiService {
    // For requests with grounding (Google Search)
    @POST("v1beta/models/gemini-2.0-flash-exp:generateContent")
    suspend fun generateContentWithGrounding(
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequestWithGrounding
    ): Response<GeminiResponse>

    // For requests with structured output (JSON schema)
    @POST("v1beta/models/gemini-2.0-flash-exp:generateContent")
    suspend fun generateContentWithSchema(
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequestWithSchema
    ): Response<GeminiResponse>
}

// Request with grounding
data class GeminiRequestWithGrounding(
    val contents: List<Content>,
    val systemInstruction: SystemInstruction? = null,
    val tools: List<Tool> = listOf(Tool(googleSearch = GoogleSearch()))
)


data class GeminiRequestWithSchema(
    val contents: List<Content>,
    val systemInstruction: SystemInstruction? = null,
    val generationConfig: GenerationConfig
)

data class SystemInstruction(
    val role: String = "user",
    val parts: List<Part>
)

// Common models
data class Content(
    val role: String = "user",
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GenerationConfig(
    val temperature: Float = 1.0f,
    val topK: Int = 40,
    val topP: Float = 0.95f,
    val maxOutputTokens: Int = 8192,
    val responseMimeType: String = "application/json",
    val responseSchema: ResponseSchema
)

data class ResponseSchema(
    val type: String = "object",
    val properties: ResponseProperties
)

data class ResponseProperties(
    val SearchQuery: SchemaProperty,
    val actions: SchemaArray
)

data class SchemaProperty(
    val type: String = "string"
)

data class SchemaArray(
    val type: String = "array",
    val items: ActionSchema
)

data class ActionSchema(
    val type: String = "object",
    val properties: ActionProperties,
    val required: List<String> = listOf("Function", "Arguments")
)

data class ActionProperties(
    val Function: SchemaProperty,
    val Arguments: ArgumentSchema
)

data class ArgumentSchema(
    val type: String = "array",
    val items: SchemaProperty
)

data class GeminiResponse(val candidates: List<Candidate>)
data class Candidate(val content: Content)

// GoogleSearch should not be a data class
class GoogleSearch
data class Tool(val googleSearch: GoogleSearch)