package com.crossevol.wordbook

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.compose.runtime.Composable
import android.provider.DocumentsContract
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import io.github.oshai.kotlinlogging.KotlinLogging
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream // Needed for SAF writing
import java.io.IOException // Needed for exception handling

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
 *
 * This function returns a file path that can be used with our writeToFile function.
 * The writeToFile function will handle this path appropriately for file operations.
 */
@Composable
actual fun getDefaultDocumentsPath(): String {
    val context = LocalContext.current
    // Use getExternalFilesDir for app-specific documents, doesn't require WRITE_EXTERNAL_STORAGE
    // This directory is cleared when the app is uninstalled.
    val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

    // Ensure the directory exists
    if (documentsDir != null && !documentsDir.exists()) {
        documentsDir.mkdirs()
    }

    return documentsDir?.absolutePath ?: context.filesDir.absolutePath // Fallback to internal files dir
}

/**
 * Open the given file path or URI in Android's file explorer
 */
actual fun openFileExplorer(directoryPath: String): Boolean {
    // --- This function remains unchanged as it's not directly related to the writing error ---
    return try {
        if (!::appContext.isInitialized) {
            logger.error { "Context not initialized, cannot open file explorer" }
            return false
        }

        // Handle differently based on whether it's a content URI or file path
        if (directoryPath.startsWith("content://")) {
            // It's already a content URI (likely a TREE URI from export)
            val directoryUri = directoryPath.toUri()
            logger.debug { "Attempting to open directory URI: $directoryUri" }

            // Use ACTION_VIEW with the tree URI. This might not work on all file managers
            // or might open a specific file manager app rather than the generic chooser.
            // Opening a *tree* URI isn't a standard ACTION_VIEW operation for folders.
            // A better approach might be needed if this consistently fails, but it's
            // separate from the writing issue.
            // To view the directory granted by a Tree URI, we should view the Document URI of the directory itself.
            val documentUri: Uri? = try {
                val treeDocumentId = DocumentsContract.getTreeDocumentId(directoryUri)
                DocumentsContract.buildDocumentUriUsingTree(directoryUri, treeDocumentId)
            } catch (e: Exception) {
                 logger.error(e) { "Error deriving document URI from tree URI $directoryUri: ${e.message}" }
                 null
            }

            if (documentUri == null) {
                logger.warn { "Could not derive document URI for tree URI: $directoryUri. Cannot open." }
                return false
            }

            logger.debug { "Attempting to view derived document URI: $documentUri" }
            val intent = Intent(Intent.ACTION_VIEW)
            // Set the data to the document URI of the directory
            intent.setData(documentUri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // Grant read permission for the intent target
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            try {
                ContextCompat.startActivity(appContext, intent, null)
                logger.info { "Started activity to view document URI: $documentUri (derived from $directoryUri)" }
                true
            } catch (e: Exception) {
                // Catch ActivityNotFoundException specifically if it occurs
                logger.error(e) { "Error viewing document URI $documentUri: ${e.message}" }
                // You could potentially try ACTION_OPEN_DOCUMENT with EXTRA_INITIAL_URI here as a fallback
                // val fallbackIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                // fallbackIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentUri)
                // fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // try { ContextCompat.startActivity(appContext, fallbackIntent, null); true } catch ...
                false // Indicate failure
            }

        } else {
            // It's a file path (likely the default app-specific path)
            val directory = File(directoryPath)
            if (!directory.exists() || !directory.isDirectory) {
                 logger.error { "Directory path does not exist or is not a directory: $directoryPath" }
                 return false
            }
            val fileUri = Uri.fromFile(directory) // Convert file path to Uri
            logger.debug { "Attempting to open directory path: $directoryPath (Uri: $fileUri)" }

            val intent = Intent(Intent.ACTION_VIEW) // Use ACTION_VIEW for directories too
            intent.setDataAndType(fileUri, "resource/folder") // Standard type for folders
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            try {
                ContextCompat.startActivity(appContext, intent, null)
                logger.info { "Started activity to view directory path: $directoryPath" }
                true
            } catch (e: Exception) {
                logger.error(e) { "Error viewing directory path $directoryPath: ${e.message}" }
                false // Indicate failure
            }
        }
    } catch (e: Exception) {
        logger.error(e) { "General error opening file explorer for path/URI '$directoryPath': ${e.message}" }
        false
    }
}


/**
 * Helper function to request a document tree URI from the user for export.
 * This launches the system's document picker to let the user select a directory.
 *
 * @param activity The activity to launch the picker from
 * @param requestCode The request code to use for the activity result
 */
fun requestDocumentTreeUri(activity: android.app.Activity, requestCode: Int) {
    try {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        // Request persistable permissions when picking the directory
        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        )
        activity.startActivityForResult(intent, requestCode)
    } catch (e: Exception) {
        logger.error(e) { "Error launching document tree picker: ${e.message}" }
    }
}


/**
 * Actual implementation for Android to write content to a file.
 * Handles both file paths and content URIs appropriately.
 * For file paths: Uses standard File I/O
 * For content URIs: Uses DocumentsContract and ContentResolver
 */
actual fun writeToFile(directoryLocation: String, baseFilename: String, extension: String, content: String): String? {
    if (!::appContext.isInitialized) {
        logger.error { "Context not initialized, cannot write file" }
        return null
    }

    // Generate timestamp for filename
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val filename = "${baseFilename}_${timestamp}.${extension.lowercase()}"

    // Determine MIME type
    val mimeType = when (extension.lowercase()) {
        "json" -> "application/json"
        "csv" -> "text/csv"
        else -> "application/octet-stream" // Default MIME type
    }

    return try {
        // Check if the directoryLocation is a content URI or a file path
        if (directoryLocation.startsWith("content://")) {
            // Handle as content URI (This should be the TREE URI from the picker)
            logger.info { "Attempting to write using SAF to directory URI: $directoryLocation" }
            val directoryUri: Uri = try {
                 directoryLocation.toUri()
            } catch (e: Exception) {
                logger.error(e) { "Failed to parse directory location string to URI: $directoryLocation" }
                return null
            }

            // Optional: Persist permissions if not already done during picking
            // This helps retain access across restarts, but shouldn't be needed for immediate write.
            // val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            // try {
            //     appContext.contentResolver.takePersistableUriPermission(directoryUri, takeFlags)
            //     logger.debug { "Successfully took persistable permissions for $directoryUri" }
            // } catch (secEx: SecurityException) {
            //     logger.warn(secEx) { "Failed to take persistable permissions for $directoryUri. This might be expected if not requested initially."}
            // }

            var fileUri: Uri? = null
            try {
                // Convert the tree URI to the document URI for the directory itself
                val treeDocumentId = DocumentsContract.getTreeDocumentId(directoryUri)
                val parentDocumentUri = DocumentsContract.buildDocumentUriUsingTree(directoryUri, treeDocumentId)

                logger.debug { "Original Tree URI: $directoryUri" }
                logger.debug { "Derived Parent Document URI: $parentDocumentUri" }
                logger.debug { "Calling DocumentsContract.createDocument with parentDocumentUri=$parentDocumentUri, mimeType=$mimeType, displayName=$filename" }

                // Create the document using DocumentsContract, passing the derived parent document URI
                fileUri = DocumentsContract.createDocument(
                    appContext.contentResolver,
                    parentDocumentUri, // Use the document URI of the directory
                    mimeType,
                    filename
                )
                logger.debug { "DocumentsContract.createDocument returned URI: $fileUri" }

            } catch (e: Exception) {
                // Catch potential exceptions during createDocument, including IllegalArgumentException
                logger.error(e) { "Error calling DocumentsContract.createDocument for parent URI $directoryUri" }
                return null // Exit if creation failed
            }


            if (fileUri == null) {
                logger.error { "Failed to create document using DocumentsContract (returned null) for parent URI: $directoryUri" }
                return null
            }

            // Write the content to the created document's OutputStream
            try {
                appContext.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                    outputStream.write(content.toByteArray(Charsets.UTF_8)) // Specify charset
                    logger.info { "Successfully wrote content to created file URI: $fileUri" }
                } ?: run {
                    logger.error { "Failed to open OutputStream for the created document URI: $fileUri" }
                    // Attempt to delete the partially created file if output stream failed
                    try {
                        if (DocumentsContract.deleteDocument(appContext.contentResolver, fileUri)) {
                             logger.info { "Deleted partially created document: $fileUri" }
                        } else {
                             logger.warn { "Failed to delete partially created document: $fileUri" }
                        }
                    } catch (deleteEx: Exception) {
                        logger.error(deleteEx) { "Error deleting partially created document: $fileUri" }
                    }
                    return null // Return null as writing failed
                }
            } catch (ioe: IOException) {
                 logger.error(ioe) { "IOException writing to OutputStream for URI: $fileUri" }
                 // Attempt deletion on IO error too
                 try {
                     DocumentsContract.deleteDocument(appContext.contentResolver, fileUri)
                 } catch (_: Exception) {}
                 return null
            }

            fileUri.toString() // Return the URI string of the created file

        } else {
            // Handle as file path (App-specific directory - this part works)
            logger.info { "Attempting to write using standard File I/O to path: $directoryLocation" }
            val directory = File(directoryLocation)
            if (!directory.exists()) {
                val created = directory.mkdirs()
                if (!created) {
                    logger.error { "Failed to create directory: $directoryLocation" }
                    return null
                }
            }

            val filePath = "${directoryLocation}${File.separator}$filename"
            val file = File(filePath)

            try {
                // Write content to file using FileOutputStream for consistency
                 FileOutputStream(file).use { outputStream ->
                     outputStream.write(content.toByteArray(Charsets.UTF_8)) // Specify charset
                 }
                logger.info { "Successfully wrote file to path: ${file.absolutePath}" }
                file.absolutePath // Return the absolute path of the created file
            } catch (ioe: IOException) {
                logger.error(ioe) { "IOException writing to file path: $filePath" }
                return null
            }
        }
    } catch (e: Exception) {
        // Catch any other unexpected errors
        logger.error(e) { "Unexpected error writing file to location '$directoryLocation': ${e.message}" }
        null
    }
}
