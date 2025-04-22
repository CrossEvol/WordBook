package com.crossevol.wordbook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info // For Introduction
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications // For Notification Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import wordbook.composeapp.generated.resources.Res // Import generated resources
import wordbook.composeapp.generated.resources.girl

/**
 * Settings Page composable based on the design image SettingsPage.png.
 *
 * @param username The username to display.
 * @param email The email address to display.
 * @param onNavigateBack Callback to navigate back (e.g., to HomePage).
 * @param onEditProfile Callback when "Edit Profile" is clicked.
 * @param onChangeApiKey Callback when "Change ApiKey" is clicked.
 * @param onNotificationSettings Callback when "Notification Settings" is clicked.
 * @param onIntroduction Callback when "Introduction" is clicked.
 * @param onTermsOfService Callback when "Terms of Services" is clicked.
 * @param onLogout Callback when "Log Out" is clicked.
 */
@Composable
fun SettingsPage(
    username: String = "[Username]", // Default values for preview
    email: String = "[Email_Address]",
    onNavigateBack: () -> Unit, // Required for navigation
    onEditProfile: () -> Unit = {},
    onChangeApiKey: () -> Unit = {},
    onNotificationSettings: () -> Unit = {},
    onIntroduction: () -> Unit = {},
    onTermsOfService: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    // Define the primary color from the image header (approximate)
    val headerColor = Color(0xFF4A148C) // Deep Purple / Indigo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Use theme background
    ) {
        // Header Section
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp), // Adjust height as needed
            color = headerColor,
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp) // Rounded bottom corners
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar Image
                Image(
                    painter = painterResource(Res.drawable.girl), // Load from common resources
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(80.dp) // Keep original size
                        .clip(RoundedCornerShape(12.dp)), // Keep original shape
                    contentScale = ContentScale.Crop // Crop to fit bounds
                )
                Spacer(modifier = Modifier.width(16.dp))
                // User Info
                Column {
                    Text(
                        text = username,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = email,
                        color = Color.White.copy(alpha = 0.8f), // Slightly transparent
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Settings Items Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Space between items
        ) {
            SettingsItem(
                icon = Icons.Filled.AccountBox, // Correct Icon
                text = "Edit Profile",
                onClick = onEditProfile
            )
            SettingsItem(
                icon = Icons.Filled.Lock, // Correct Icon
                text = "Change ApiKey",
                onClick = onChangeApiKey
            )
            SettingsItem(
                icon = Icons.Filled.Notifications,
                text = "Notification Settings",
                onClick = onNotificationSettings
            )
            SettingsItem(
                icon = Icons.Filled.Info,
                text = "Introduction",
                onClick = onIntroduction
            )
            SettingsItem(
                icon = Icons.AutoMirrored.Filled.Send, // Correct Icon
                text = "Terms of Services",
                onClick = onTermsOfService
            )
        }

        Spacer(modifier = Modifier.weight(1f)) // Push logout button to bottom

        // Log Out Button
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp) // More padding around button
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant, // Light background for button
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant // Text color
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ExitToApp, // Correct Icon
                contentDescription = null, // Button text is descriptive
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text("Log Out", fontSize = 16.sp)
        }
    }
}

/**
 * Reusable composable for each setting item row.
 */
@Composable
private fun SettingsItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // White/Surface background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Subtle shadow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp), // Padding inside the card
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null, // Text describes the action
                tint = MaterialTheme.colorScheme.onSurfaceVariant // Icon color
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface // Text color
            )
        }
    }
}


@Preview
@Composable
fun SettingsPagePreview() {
    MaterialTheme {
        SettingsPage(onNavigateBack = {}) // Provide dummy lambda for preview
    }
}
