package com.crossevol.wordbook.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * A simpler styled item for displaying a related word.
 */
@Composable
fun RelatedWordItem(
    text: String,
    onClick: () -> Unit
) {
    // Use a simple Text with padding and clickable modifier
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium, // Slightly smaller text than sentences
        color = MaterialTheme.colorScheme.onSurfaceVariant, // Use a secondary color
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 16.dp,
                vertical = 4.dp
            ) // Add padding
    )
}

@Preview
@Composable
fun RelatedWordItemPreview() {
    MaterialTheme {
        RelatedWordItem(
            text = "abc",
            onClick = {},
        )
    }
}

