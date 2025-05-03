package com.crossevol.wordbook

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}
private lateinit var appContext: Context

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

/**
 * Get information about the Android platform
 */
actual fun getPlatform(): Platform = AndroidPlatform()

/**
 * Initialize the Android platform extension with the application context
 */
fun initPlatformExt(context: Context) {
    appContext = context.applicationContext
}

/**
 * Actual implementation for Android to get the default documents directory path.
 * Uses getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) which is app-specific
 * and doesn't require extra permissions. Falls back to internal files dir if external fails.
 */
@Composable
actual fun getDefaultDocumentsPath(): String {
    val context = LocalContext.current
    // Use getExternalFilesDir for app-specific documents, doesn't require WRITE_EXTERNAL_STORAGE
    // This directory is cleared when the app is uninstalled.
    // For shared documents, Storage Access Framework or MediaStore is preferred,
    // but getExternalFilesDir is simpler for a default path for export.
    val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    return documentsDir?.absolutePath ?: context.filesDir.absolutePath // Fallback to internal files dir
}

/**
 * Open the given file path in Android's file explorer
 */
actual fun openFileExplorer(directoryPath: String): Boolean {
    return try {
        if (!::appContext.isInitialized) {
            logger.error { "Context not initialized, cannot open file explorer" }
            return false
        }
        
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:${directoryPath.substringAfterLast('/')}")
        intent.setDataAndType(uri, "resource/folder")
        
        // Use FLAG_ACTIVITY_NEW_TASK for app context
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        
        ContextCompat.startActivity(appContext, intent, null)
        true
    } catch (e: Exception) {
        logger.error(e) { "Error opening file explorer: ${e.message}" }
        
        // Try alternative approach
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setDataAndType(Uri.parse(directoryPath), "*/*")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            appContext.startActivity(intent)
            return true
        } catch (e2: Exception) {
            logger.error(e2) { "Error opening file explorer (alternate method): ${e2.message}" }
            false
        }
    }
}
