package com.crossevol.wordbook

import android.os.Build
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.File

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

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
