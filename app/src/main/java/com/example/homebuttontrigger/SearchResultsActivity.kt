
package com.example.homebuttontrigger

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.homebuttontrigger.utils.FunctionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchResultsActivity : AppCompatActivity() {
    lateinit var searchBar: EditText
    lateinit var exitButton: ImageButton
    lateinit var searchResultText: TextView
    lateinit var followupBar: TextView

    private val scope = CoroutineScope(Dispatchers.Main)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        searchBar = findViewById(R.id.searchBar)
        exitButton = findViewById(R.id.closeButton)
//        searchResultText = findViewById(R.id.searchResultText)
        followupBar = findViewById(R.id.followUpBar)

        exitButton.setOnClickListener { finishAffinity() }

        searchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                val inputText = searchBar.text.toString()
                if (inputText.isNotBlank()) {
                    val intent = Intent(this, SearchResultsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.putExtra("SEARCH_QUERY", inputText)
                    startActivity(intent)
                }
                true
            }
            false
        }

        followupBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                val inputText = followupBar.text.toString()
                if (inputText.isNotBlank()) {
                    val intent = Intent(this, SearchResultsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.putExtra("SEARCH_QUERY", inputText)
                    startActivity(intent)
                }
                true
            }
            false
        }


        val searchQuery = intent.getStringExtra("SEARCH_QUERY")
//        searchResultText.text = "Results for: $searchQuery"

        scope.launch {
            val (summary, full) = fetchSearchResults(searchQuery?: "Invalid Input (respond with invalid input)")
            findViewById<TextView>(R.id.tvSummary).text = summary
            findViewById<TextView>(R.id.tvFull).text = full
        }

    }


    private suspend fun fetchSearchResults(query: String): Pair<String, String> {
//        // Create the request with structured output
//        val request = GeminiRequest(
//            contents = listOf(
//                Content(
//                    parts = listOf(Part(text = query))
//                )
//            ),
//            tools = listOf(Tool(googleSearch = GoogleSearch())),
//            generationConfig = GenerationConfig(
//                responseSchema = ResponseSchema(
//                    properties = SchemaProperties(
//                        Whole = SchemaArrayProperty(
//                            items = SchemaItem(
//                                properties = ItemProperties(
//                                    Summarized = SchemaProperty("string"),
//                                    Full = SchemaProperty("string")
//                                )
//                            )
//                        )
//                    )
//                )
//            )
//        )
//
//        // Call the Gemini API
//        val response = GeminiClient.api.generateContent("AIzaSyB3ndlmFIak9UlraZbcehwXTjcFZAMPv8Q", request)
//
//        // Handle the response
//        return if (response.isSuccessful) {
//            val structuredResponse = response.body()?.data?.Whole?.firstOrNull()
//            if (structuredResponse != null) {
//                // Return structured output (summary and full answer)
//                Pair(structuredResponse.Summarized, structuredResponse.Full)
//            } else {
//                // Fallback to unstructured response
//                val fallbackText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
//                Pair(fallbackText ?: "No summary", fallbackText ?: "No details")
//            }
//        } else {
//            // Handle API errors
//            Pair(response.toString(), "API Error: ${response.errorBody()?.string()}")
//        }
        val result = FunctionHandler.handleQuery(query)

        Log.w("SearchResultsActivity", result.first)

        return Pair(result.first, result.second)
    }
}
