package com.crossevol.wordbook.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A styled item for displaying a sentence, matching the design.
 */
@Composable
fun SentenceItem(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp), // Rounded corners
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f) // Light background
//        ),
//        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // No shadow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Push arrow to the end
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.weight(1f) // Allow text to take space
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "View sentence detail", // Accessibility
                tint = MaterialTheme.colors.onSurface
            )
        }
    }
}
