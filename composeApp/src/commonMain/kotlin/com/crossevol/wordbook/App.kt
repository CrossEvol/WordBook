package com.crossevol.wordbook

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.crossevol.wordbook.data.model.WordItem
import com.crossevol.wordbook.ui.screens.HomePage
import com.crossevol.wordbook.ui.screens.WordDetailPage // Import the new Detail Page
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun App() {
    // Simple navigation state: null means home, non-null means detail page
    var currentWordDetail by remember { mutableStateOf<WordItem?>(null) }

    MaterialTheme {
        if (currentWordDetail == null) {
            // Show Home Page
            HomePage(
                onWordItemClick = { word ->
                    currentWordDetail = word // Navigate to detail view
                },
                onNavigate = { route ->
                    // Handle bottom navigation clicks if needed (e.g., clear detail view)
                    println("Bottom nav clicked: $route")
                    if (route != "home") { // Example: Clear detail if navigating away from home section
                       // currentWordDetail = null // Decide if bottom nav should clear detail view
                    }
                }
            )
        } else {
            // Show Word Detail Page
            WordDetailPage(
                wordItem = currentWordDetail!!, // We know it's not null here
                onBack = {
                    currentWordDetail = null // Navigate back to home
                }
            )
        }
    }
}
