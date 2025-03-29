
package com.example.homebuttontrigger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.homebuttontrigger.utils.ActivityContextHolder


class MainActivity : AppCompatActivity() {
    lateinit var searchBar: EditText
    lateinit var exitButton: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityContextHolder.currentActivity = this
        // Safe check for layout
        try {
            setContentView(R.layout.activity_main)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting content view", e)
        }

        // Verify resources exist
        searchBar = findViewById(R.id.searchBar)
        exitButton= findViewById(R.id.closeButton)
        // Null checks
        exitButton.setOnClickListener {
            finishAffinity()
        }
        searchBar.setOnEditorActionListener { _, actionId, _ ->
            handleSearchAction(actionId)
        }
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

    override fun onDestroy() {
        super.onDestroy()
        ActivityContextHolder.currentActivity = null // Clear reference to prevent leaks
    }

}