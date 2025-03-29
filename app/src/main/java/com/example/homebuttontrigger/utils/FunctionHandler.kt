package com.example.homebuttontrigger.utils

import android.util.Log
import com.example.homebuttontrigger.network.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ActionResponse(
    val SearchQuery: String? = null,
    val actions: List<Action>? = null
)

@Serializable
data class Action(
    val Function: String,
    val Arguments: List<String>
)

val json = Json { ignoreUnknownKeys = true }

fun parseJson(jsonString: String): ActionResponse? {
    return try {
        json.decodeFromString(jsonString)
    } catch (e: Exception) {
        Log.e("OnDevice", "JSON Parsing Error: ${e.message}")
        null
    }
}

val SYSTEM_INSTRUCTIONS = """
    Categorize the prompt. Determine if it requires searching the internet, performing device actions, or both.
        
        Rules:
        1. Chats with the bot are considered to be searchQuery(s). Eg.: "Hello", "How are you"?
        2. If a search is required, return it as "SearchQuery".
        3. If one or more actions (function calls) are required, return them in "actions".  
        4. If an action (like sending a message) needs a search result as an argument, use "{{SearchResult}}" instead of the full query text.
        5. If there are no actions required, omit "actions" from the response.
        6. If no search is required, omit "SearchQuery" from the response.
        7. There can't be a situation where there are neither searchQuery nor action. One of Two is required.
        
        Available functions with their syntax:
        - Call(Contact,Medium ) // Call("Alan","Phone" ) Only Phone and whatsapp is available 
        - Open(ApplicationName ) // Open("Instagram")
        - Send(Message, Medium , Contact) // Send("The current price of Google stock is {{SearchResult}}","WhatsApp", "Alan")
        - Timer(time:seconds) // Timer("600")
        
        Strict Rules:
        - Do not invent new functions. Only use the functions listed above.
        - Do not invent arguments. Use the exact arguments provided in the query.
        - All arguments are necessary. Don't leave any unfilled.
        - If the query contains a search and an action, ensure the action uses "{{SearchResult}}" for the search-dependent argument.
""".trimIndent()

val SEARCH_INSTRUCTIONS = """Answer strictly in the following format:
If the question is "Current President of the US", respond as follows:

Answer:

Summarized: ""${'"'}Donald Trump""${'"'}
Full: ""${'"'}The current president of the United States is Donald J. Trump. He is the 47th president and was sworn into office on January 20, 2025. J.D. Vance is the current Vice President.""'
Ensure:


The summarized response is concise and to the point (e.g., just a name, date, or key fact).
The full response is detailed and well-structured, citing sources in [number] format.
The format is strictly followed with no additional commentary.
Make sure to surround summarized value and full value with triple quotations.
The response doesn't have wrong details""".trimIndent()

object FunctionHandler {
    private const val API_KEY = "AIzaSyB3ndlmFIak9UlraZbcehwXTjcFZAMPv8Q"
    private fun onDevice(): OnDevice {
        return OnDevice()
    }

    suspend fun handleQuery(query: String): Pair<String, String> {
        val result = singleIntent(query)

        // If singleIntent was handled, return the result; otherwise, use multi-intent logic
        return result ?: handleElse(query)
    }


    private fun singleIntent(query: String): Pair<String, String>? {
        val words = query.split(" ")
        Log.w("OnDevice", "Reached singleIntent with query: $query")

        // Check if the query starts with "call", "open", or "send"
        return when (words.firstOrNull()?.lowercase()) {
            "call" -> {
                if (words.size >= 2) {
                    val name = words.subList(1, words.size).joinToString(" ")
                    Log.w("OnDevice", "Calling: $name")
                    onDevice().call(name)
                    Pair("call", name)
                } else {
                    Log.i("InvalidFormat", "Invalid call format. Use: call <name>")
                    null
                }
            }

            "open" -> {
                if (words.size >= 2) {
                    Log.w("OnDevice", "Opening: ${words[1]}")
                    onDevice().openApp(words[1])
                    Pair("open", words[1])
                } else {
                    Log.i("InvalidFormat", "Invalid open format. Use: open <app_name>")
                    null
                }
            }

            "send" -> {
                if (words.size >= 3) {
                    val message = words.subList(1, words.size - 1).joinToString(" ")
                    val name = words.last()
                    Log.w("OnDevice", "Sending message: \"$message\" to $name")
                    onDevice().send(name, message)
                    Pair("send", "$message to $name")
                } else {
                    Log.i("InvalidFormat", "Invalid send format. Use: send <message> <name>")
                    null
                }
            }

            else -> null // If it doesn’t start with "call", "open", or "send", return null so multi-intent can handle it
        }
    }

    private suspend fun handleElse(query: String): Pair<String, String> {
        val jsonResponse = generateStructuredOutput(query)
        Log.w("OnDevice", "Raw JSON Response: $jsonResponse")

        var (summarized, full) = Pair("No Search Query", "No actions to perform")
        var returns = Pair(summarized, full) // Initialize `returns` here

        if (jsonResponse != null) {
            if (jsonResponse.SearchQuery != null) {
                val id = fetchSearchResult(query)
                summarized = id.first
                full = id.second
                returns = Pair(summarized, full)
                Log.w("OnDevice", "Search Result: $full")
            } else {
                Log.w("OnDevice", "No Search Query")
            }

            jsonResponse.actions?.forEach { action ->
                executeAction(action, full)
            }
        }

        return returns
    }


    private suspend fun generateStructuredOutput(query: String): ActionResponse? {

        val request = GeminiRequestWithSchema(
            contents = listOf(Content(parts = listOf(Part(text = query)))),
            systemInstruction = SystemInstruction(
                parts = listOf(Part(text = SYSTEM_INSTRUCTIONS))
            ),
            generationConfig = GenerationConfig(
                temperature = 0f, // Set temperature to 0
                topK = 64,
                topP = 0.95f,
                maxOutputTokens = 8192,
                responseMimeType = "application/json",
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
        val response = GeminiClient.api.generateContentWithSchema(API_KEY, request)
        val jsonResponse =
            response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        return jsonResponse?.let { parseJson(it) }
    }

    private suspend fun fetchSearchResult(query: String): Pair<String, String> {
        val request = GeminiRequestWithGrounding(
            contents = listOf(Content(parts = listOf(Part(text = query)))),
            systemInstruction = SystemInstruction(
                parts = listOf(Part(text = SEARCH_INSTRUCTIONS))
            ),
            tools = listOf(Tool(googleSearch = GoogleSearch())),
        )
        val response = GeminiClient.api.generateContentWithGrounding(API_KEY, request)
        val result = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        val summarized = result?.substringAfter("Summarized: ")?.substringBefore("Full:")?.trim()
            ?.removeSurrounding("\"\"\"")
        val full = result?.substringAfter("Full: ")?.trim()?.removeSurrounding("\"\"\"")
        Log.w("OnDevice", "Search Result: $summarized")
        Log.w("OnDevice", "Full Result: $full")

        return if (response.isSuccessful) {
            Pair(
                """${summarized ?: "Error: Empty response"}, ${full ?: "Error: Empty response"}""",
                result.toString()
            )
        } else {
            Pair("Error: ${response.errorBody()?.string()}", "error)")
        }
    }

    private fun executeAction(action: Action, searchResult: String?) {
        val arguments = action.Arguments.map { arg ->
            if (arg == "{{SearchResult}}") searchResult ?: "No search result" else arg
        }

        when (action.Function.lowercase()) {
            "call" -> {
                onDevice().call(arguments[0])

                Log.e("OnDevice", "Invalid arguments for Call: ${arguments.joinToString()}")

            }

            "open" -> {
                if (arguments.size == 1) {
                    onDevice().openApp(arguments[0])
                } else {
                    Log.e("OnDevice", "Invalid arguments for Open: ${arguments.joinToString()}")
                }
            }

            "send" -> {
                if (arguments.size == 3) {
                    onDevice().send(arguments[1], arguments[0])
                } else {
                    Log.e("OnDevice", "Invalid arguments for Send: ${arguments.joinToString()}")
                }
            }

            else -> {
                Log.e("OnDevice", "Unknown function: ${action.Function}")
            }
        }
    }
}