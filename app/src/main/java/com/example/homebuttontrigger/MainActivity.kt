
package com.example.homebuttontrigger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Safe check for layout
        try {
            setContentView(R.layout.activity_main)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting content view", e)
        }

        // Verify resources exist
        val searchBar: EditText? = findViewById(R.id.searchBar)
        val exitButton: ImageButton? = findViewById(R.id.closeButton)

        // Null checks
        exitButton?.setOnClickListener {
            finishAffinity()
        }

        searchBar?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                val inputText = searchBar.text.toString()
                if (inputText.isNotBlank()) {
                    val intent = Intent(this, SearchResultsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clears the overlay and starts fresh
                    intent.putExtra("SEARCH_QUERY", inputText)
                    startActivity(intent)
                }
                true
            } else {
                false
            }
        }

    }

}