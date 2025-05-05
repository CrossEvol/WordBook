package com.crossevol.wordbook

import androidx.compose.runtime.Composable
import io.github.oshai.kotlinlogging.KotlinLogging
// Removed java.io.File import as it's not used directly here anymore

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
 * Platform-specific function to write content to a file in a specified location.
 * On Android, this handles writing to a `content://` URI obtained via SAF.
 * On Desktop, this writes to a standard file path.
 * Handles filename generation with timestamp.
 *
 * @param directoryLocation The target directory path (Desktop) or URI string (Android).
 * @param baseFilename The base name for the file (e.g., "wordbook_export").
 * @param extension The file extension (e.g., "json", "csv").
 * @param content The string content to write to the file.
 * @return The full path or URI string of the created file, or null on failure.
 */
expect fun writeToFile(directoryLocation: String, baseFilename: String, extension: String, content: String): String?

/**
 * Platform-specific function to read the content of a file.
 * On Android, this handles reading from a `content://` URI obtained via the file picker.
 * On Desktop, this reads from a standard file path.
 *
 * @param filePath The path (Desktop) or URI string (Android) of the file to read.
 * @return The content of the file as a String, or null on failure.
 */
expect fun readFileContent(filePath: String): String?

/**
 * Platform-specific function to display a simple notification.
 *
 * @param title The title of the notification.
 * @param message The main text content of the notification.
 * @param onClick An optional callback to be executed when the notification is clicked.
 */
expect fun showNotification(title: String, message: String, onClick: (() -> Unit)? = null)


/**
 * Class to hold platform-specific information
 */
data class PlatformInfo(
    val name: String,
    val version: String
)


/**
 * Expects a platform-specific implementation for displaying a time picker dialog.
 *
 * @param show Controls the visibility of the time picker.
 * @param initialHour The initial hour to display (0-23).
 * @param initialMinute The initial minute to display (0-59).
 * @param onDismiss Called when the dialog is dismissed without selecting a time.
 * @param onTimeSelected Called with the selected hour and minute when the user confirms.
 */
@Composable
expect fun PlatformTimePicker(
    show: Boolean,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
)
