package com.crossevol.wordbook

// Import Material 3 Theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.crossevol.wordbook.ui.screens.HomePage // Import the new HomePage
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun App() {
    // Apply Material 3 Theme
    MaterialTheme {
        // Display the HomePage as the main content of the application
        HomePage()
    }
}
