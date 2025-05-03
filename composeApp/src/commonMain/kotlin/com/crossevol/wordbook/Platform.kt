package com.crossevol.wordbook

import androidx.compose.runtime.Composable
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

interface Platform {
    val name: String
}

/**
 * Get information about the current platform
 * @return A PlatformInfo object with platform details
 */
expect fun getPlatform(): Platform

/**
 * Expect function to get the default documents directory path for the current platform.
 * This function is marked as Composable because its Android implementation requires
 * access to the Android Context via LocalContext.current.
 */
@Composable
expect fun getDefaultDocumentsPath(): String

/**
 * Open the given file path in the platform's default file explorer.
 * This is a multiplatform function that will be called from common code.
 *
 * @param directoryPath The directory path to open
 * @return True if successful, false otherwise
 */
expect fun openFileExplorer(directoryPath: String): Boolean

/**
 * Class to hold platform-specific information
 */
data class PlatformInfo(
    val name: String,
    val version: String
)
