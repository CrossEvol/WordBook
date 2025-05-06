package com.crossevol.wordbook.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
//import androidx.compose.desktop.ui.tooling.preview.Preview

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
        style = MaterialTheme.typography.caption, // Slightly smaller text than sentences
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 16.dp,
                vertical = 4.dp
            ) // Add padding
    )
}

//@Preview
@Composable
fun RelatedWordItemPreview() {
    MaterialTheme {
        RelatedWordItem(
            text = "abc",
            onClick = {},
        )
    }
}

