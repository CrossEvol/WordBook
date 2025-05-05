package com.crossevol.wordbook

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import dev.darkokoa.datetimewheelpicker.WheelTimePicker
import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.Desktop
import java.awt.Image
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.TrayIcon.MessageType
import java.io.File
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.datetime.LocalTime // Import LocalTime

private val logger = KotlinLogging.logger {}

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

/**
 * Get information about the desktop platform
 */
actual fun getPlatform(): Platform = JVMPlatform()

/**
 * Actual implementation for Desktop (JVM) to get the default documents directory path.
 * Uses the user's home directory and appends "Documents".
 */
@Composable
actual fun getDefaultDocumentsPath(): String {
    return try {
        val userHome = System.getProperty("user.home")
        val documentsPath = Paths.get(
            userHome,
            "Documents"
        ).toString()

        // Create directory if it doesn't exist
        val documents = File(documentsPath)
        if (!documents.exists()) {
            documents.mkdirs()
        }

        documentsPath
    } catch (e: Exception) {
        logger.error(e) { "Error getting documents path: ${e.message}" }

        // Fallback to user home directory
        System.getProperty("user.home")
    }
}

/**
 * Actual implementation for Desktop (JVM) to read content from a standard file path.
 */
actual fun readFileContent(filePath: String): String? {
    return try {
        val file = File(filePath)
        if (!file.exists() || !file.isFile) {
            logger.error { "File does not exist or is not a file: $filePath" }
            return null
        }
        val content = file.readText(Charsets.UTF_8) // Read content using UTF-8
        logger.info { "Successfully read file: $filePath" }
        content
    } catch (e: Exception) {
        logger.error(e) { "Error reading file from path $filePath: ${e.message}" }
        null
    }
}


/**
 * Actual implementation of PlatformTimePicker for Desktop using the compose-datetime-wheel-picker library.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun PlatformTimePicker(
    show: Boolean,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    if (show) {
        // State to hold the selected time from the picker
        var selectedHour by remember { mutableStateOf(initialHour) }
        var selectedMinute by remember { mutableStateOf(initialMinute) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Time") },
            text = {
                // Use the WheelTimePicker composable from the new library
                WheelTimePicker(
                    // Pass initial time as LocalTime
                    startTime = LocalTime(hour = initialHour, minute = initialMinute),
                    // Update state when time changes using the LocalTime object
                    onSnappedTime = { snappedTime ->
                        selectedHour = snappedTime.hour
                        selectedMinute = snappedTime.minute
                    }
                    // You can add other parameters like is24HourFormat = true/false if needed
                )
            },
            confirmButton = {
                Button(onClick = {
                    // Call the onTimeSelected callback with the selected time
                    onTimeSelected(selectedHour, selectedMinute)
                    onDismiss() // Dismiss the dialog after selection
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Actual implementation for Desktop (JVM) to show a notification using SystemTray.
 */
actual fun showNotification(title: String, message: String) {
    if (!SystemTray.isSupported()) {
        logger.warn { "SystemTray is not supported on this platform. Cannot show notification." }
        // Fallback: Log to console as a simple alternative
        println("Notification: [$title] $message")
        return
    }

    val tray = SystemTray.getSystemTray()

    // Load an image icon (make sure you have an icon resource)
    // Place 'word_book_icon.png' (or your icon) in 'composeApp/src/commonMain/resources/drawable/'
    // Or adjust the path if placed elsewhere.
    val image: Image? = try {
        // Attempt to load from resources relative to the classpath root
        val resourceUrl = ClassLoader.getSystemResource("word_book_icon.png")
        if (resourceUrl != null) {
            Toolkit.getDefaultToolkit().getImage(resourceUrl)
        } else {
            logger.error { "Notification icon resource not found: drawable/word_book_icon.png" }
            null
        }
    } catch (e: Exception) {
        logger.error(e) { "Error loading notification icon: ${e.message}" }
        null
    }

    // Create a TrayIcon (even if temporary just to display the message)
    val trayIcon = TrayIcon(image ?: createDefaultImage(), "WordBook Notification") // Provide a fallback image if loading fails
    trayIcon.isImageAutoSize = true // Adjust icon size automatically
    tray.add(trayIcon)

    // Display the message
    // Note: This doesn't add a persistent icon to the tray, just shows the popup.
    trayIcon.displayMessage(title, message, MessageType.INFO)
    logger.info { "Desktop notification displayed: '$title' - '$message'" }

    tray.remove(trayIcon)
}

// Helper function to create a minimal default image if icon loading fails
private fun createDefaultImage(): Image {
    // Create a tiny transparent image as a fallback
    return java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB)
}

/**
 * Open the given directory path in the desktop file explorer
 */
actual fun openFileExplorer(directoryPath: String): Boolean {
    return try {
        val directory = File(directoryPath)

        // Check if Desktop API is supported
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            Desktop.getDesktop().open(directory)
            true
        } else {
            // Desktop API not supported, fall through to platform-specific commands
            logger.warn { "Desktop API not supported, trying platform-specific commands." }
            false
        }
    } catch (e: Exception) {
        logger.error(e) { "Error opening file explorer using Desktop API: ${e.message}" }

        // Try alternative methods
        try {
            // Try using platform-specific commands
            val os = System.getProperty("os.name").lowercase()
            val command = when {
                os.contains("win") -> arrayOf(
                    "explorer.exe",
                    directoryPath
                )

                os.contains("mac") -> arrayOf(
                    "open",
                    directoryPath
                )

                os.contains("nix") || os.contains("nux") -> arrayOf(
                    "xdg-open",
                    directoryPath
                )

                else -> null
            }

            if (command != null) {
                Runtime.getRuntime().exec(command)
                true
            } else {
                false
            }
        } catch (e2: Exception) {
            logger.error(e2) { "Error opening file explorer (alternative method): ${e2.message}" }
            false
        }
    }
}


/**
 * Actual implementation for Desktop (JVM) to write content to a standard file.
 */
actual fun writeToFile(directoryLocation: String, baseFilename: String, extension: String, content: String): String? {
    return try {
        // Ensure directory exists (it should, based on getDefaultDocumentsPath)
        val directory = File(directoryLocation)
        if (!directory.exists()) {
            directory.mkdirs()
        }

        // Generate timestamp for filename
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "${baseFilename}_${timestamp}.${extension.lowercase()}"
        val filePath = "$directoryLocation${File.separator}$filename"

        val file = File(filePath)
        file.writeText(content, Charsets.UTF_8) // Write content using UTF-8

        logger.info { "Successfully wrote file: ${file.absolutePath}" }
        file.absolutePath // Return the absolute path of the created file

    } catch (e: Exception) {
        logger.error(e) { "Error writing file to path $directoryLocation: ${e.message}" }
        null
    }
}
