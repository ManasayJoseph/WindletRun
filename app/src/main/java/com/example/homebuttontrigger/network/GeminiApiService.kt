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
    @POST("v1beta/models/gemini-2.0-flash-exp:generateContent")
    suspend fun generateContent(
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}

// Updated Request Model
data class GeminiRequest(
    val contents: List<Content>,
    val tools: List<Tool> = listOf(Tool(googleSearch = GoogleSearch()))
)

data class Content(val parts: List<Part>)
data class Part(val text: String)

// GoogleSearch should not be a data class
class GoogleSearch
data class Tool(val googleSearch: GoogleSearch)

// Response Model
data class GeminiResponse(val candidates: List<Candidate>)
data class Candidate(val content: Content)
