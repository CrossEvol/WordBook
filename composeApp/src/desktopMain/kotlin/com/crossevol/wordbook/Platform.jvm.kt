package com.crossevol.wordbook

import androidx.compose.runtime.Composable
import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.Desktop
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
