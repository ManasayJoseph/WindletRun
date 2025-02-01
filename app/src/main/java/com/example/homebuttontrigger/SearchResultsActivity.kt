//package com.example.homebuttontrigger
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.inputmethod.EditorInfo
//import android.widget.EditText
//import android.widget.ImageButton
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//
//class SearchResultsActivity : AppCompatActivity() {
//    lateinit var searchBar: EditText
//    lateinit var exitButton: ImageButton
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_search_results)
//        searchBar = findViewById(R.id.searchBar)
//        exitButton = findViewById(R.id.closeButton)
//        // Null checks
//        exitButton.setOnClickListener {
//            finishAffinity()
//        }
//
//        searchBar.setOnEditorActionListener { _, actionId, _ ->
//            handleSearchAction(actionId)
//        }
//
//        val searchQuery = intent.getStringExtra("SEARCH_QUERY")
//        val searchResultText: TextView = findViewById(R.id.searchResultText)
//        searchResultText.text = "Results for: $searchQuery" // Placeholder for future results
//    }
//    private fun handleSearchAction(actionId: Int): Boolean {
//        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
//            val inputText = searchBar.text.toString()
//            if (inputText.isNotBlank()) {
//                val intent = Intent(this, SearchResultsActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clears the overlay and starts fresh
//                intent.putExtra("SEARCH_QUERY", inputText)
//                startActivity(intent)
//            }
//            return true
//        } else {
//            return false
//        }
//    }
//}
package com.example.homebuttontrigger

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.Tool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchResultsActivity : AppCompatActivity() {
    lateinit var searchBar: EditText
    lateinit var exitButton: ImageButton
    lateinit var searchResultText: TextView
    lateinit var followupBar: TextView

    private val scope = CoroutineScope(Dispatchers.Main)

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash-exp", // Corrected parameter name
        apiKey = "AIzaSyB3ndlmFIak9UlraZbcehwXTjcFZAMPv8Q"
    )
    private val chat = model.startChat() // Fixed function name
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        searchBar = findViewById(R.id.searchBar)
        exitButton = findViewById(R.id.closeButton)
        searchResultText = findViewById(R.id.searchResultText)
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
        searchResultText.text = "Results for: $searchQuery"

        scope.launch {
            val searchResults = fetchSearchResults(searchQuery ?: "")
            searchResultText.text = searchResults
        }
    }


    private suspend fun fetchSearchResults(query: String): String {
        val response = chat.sendMessage(query) // Added await()
        return response.text ?: "No response"
    }
}
