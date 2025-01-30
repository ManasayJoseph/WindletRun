package com.example.homebuttontrigger

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SearchResultsActivity : AppCompatActivity() {
    lateinit var searchBar: EditText
    lateinit var exitButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)
        searchBar = findViewById(R.id.searchBar)
        exitButton = findViewById(R.id.closeButton)
        // Null checks
        exitButton.setOnClickListener {
            finishAffinity()
        }

        searchBar.setOnEditorActionListener { _, actionId, _ ->
            handleSearchAction(actionId)
        }

        val searchQuery = intent.getStringExtra("SEARCH_QUERY")
        val searchResultText: TextView = findViewById(R.id.searchResultText)
        searchResultText.text = "Results for: $searchQuery" // Placeholder for future results
    }
    private fun handleSearchAction(actionId: Int): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
            val inputText = searchBar.text.toString()
            if (inputText.isNotBlank()) {
                val intent = Intent(this, SearchResultsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clears the overlay and starts fresh
                intent.putExtra("SEARCH_QUERY", inputText)
                startActivity(intent)
            }
            return true
        } else {
            return false
        }
    }
}
