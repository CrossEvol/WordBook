package com.crossevol.wordbook

import androidx.compose.runtime.Composable

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

/**
 * Expect function to get the default documents directory path for the current platform.
 * This function is marked as Composable because its Android implementation requires
 * access to the Android Context via LocalContext.current.
 */
@Composable // <-- Added @Composable here
expect fun getDefaultDocumentsPath(): String
