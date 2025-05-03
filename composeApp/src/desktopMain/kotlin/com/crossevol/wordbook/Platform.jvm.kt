package com.crossevol.wordbook

import androidx.compose.runtime.Composable
import java.nio.file.FileSystems
import java.nio.file.Paths

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

/**
 * Actual implementation for Desktop (JVM) to get the default documents directory path.
 * Uses the user's home directory and appends "Documents".
 */
@Composable // <-- Added @Composable here to match the expect signature
actual fun getDefaultDocumentsPath(): String {
    // Get the user's home directory
    val userHome = System.getProperty("user.home") ?: ""
    if (userHome.isBlank()) {
        return "" // Return empty if home directory is not found
    }

    // Construct the path to the Documents folder
    // Use Paths.get for platform-independent path construction
    val documentsPath = Paths.get(userHome, "Documents")

    // Return the absolute path string
    return documentsPath.toString()
}
