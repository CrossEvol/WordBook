package com.crossevol.wordbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
 * @param onEdit Callback for the "Edit" action.
 * @param onDelete Callback for the "Delete" action.
 * @param onCancel Callback for the "Cancel" action.
 * @param modifier Modifier for the Column layout.
 */
@Composable
fun SimpleActionSheet(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)), // Rounded top corners
        color = MaterialTheme.colorScheme.surface, // Or a specific background color if needed
        shadowElevation = 4.dp // Add some elevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Padding around the buttons
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp) // Space between buttons
        ) {
            // Edit Button (Teal color)
            Button(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4DB6AC) // Teal color from image
                )
            ) {
                Text("Edit Post") // Text from image
            }

            // Delete Button (Red color)
            Button(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF06292) // Pink/Red color from image
                )
            ) {
                Text("Delete Story") // Text from image
            }

            // Cancel Button (Light Gray color)
            Button(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE0E0E0), // Light gray from image
                    contentColor = Color.Black.copy(alpha = 0.7f) // Darker text for contrast
                )
            ) {
                Text("Cancel") // Text from image
            }
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
