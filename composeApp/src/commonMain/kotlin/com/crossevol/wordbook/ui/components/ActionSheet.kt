package com.crossevol.wordbook.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * A simple action sheet composable based on the design image Action2SheetSimple.png.
 *
 * @param onEdit Callback for the "Remember" action.
 * @param onDelete Callback for the "Forget" action.
 * @param onCancel Callback for the "Skip" action.
 * @param editLabel Custom label for the edit button (default: "Remember").
 * @param deleteLabel Custom label for the delete button (default: "Forget").
 * @param cancelLabel Custom label for the cancel button (default: "Skip").
 * @param modifier Modifier for the Column layout.
 */
@Composable
fun SimpleActionSheet(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    editLabel: String = "Remember",
    deleteLabel: String = "Forget",
    cancelLabel: String = "Skip",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)), // Rounded top corners
        color = MaterialTheme.colors.surface, // Or a specific background color if needed
        elevation = 8.dp // Increased elevation for more prominence
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp), // Increased padding for larger sheet
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Increased space between buttons
        ) {
            // Remember Button (Teal color)
            Button(
                onClick = onEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), // Taller buttons
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF4DB6AC) // Teal color from image
                )
            ) {
                Text(
                    editLabel, 
                    style = MaterialTheme.typography.button // Larger text
                )
            }

            // Forget Button (Red color)
            Button(
                onClick = onDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), // Taller buttons
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFFF06292) // Pink/Red color from image
                )
            ) {
                Text(
                    deleteLabel, 
                    style = MaterialTheme.typography.button // Larger text
                )
            }

            // Skip Button (Light Gray color)
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), // Taller buttons
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFFE0E0E0), // Light gray from image
                    contentColor = Color.Black.copy(alpha = 0.7f) // Darker text for contrast
                )
            ) {
                Text(
                    cancelLabel, 
                    style = MaterialTheme.typography.button // Larger text
                )
            }
            
            // Add some space at the bottom
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview
@Composable
fun SimpleActionSheetPreview() {
    MaterialTheme {
        SimpleActionSheet(onEdit = {}, onDelete = {}, onCancel = {})
    }
}
