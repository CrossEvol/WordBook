package com.crossevol.wordbook

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.crossevol.wordbook.db.DriverFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.russhwolf.settings.SharedPreferencesSettings // Import SharedPreferencesSettings
import io.ktor.client.* // Import HttpClient
import io.ktor.client.engine.android.* // Import Android engine
import io.ktor.client.plugins.contentnegotiation.* // Import ContentNegotiation
import io.ktor.serialization.kotlinx.json.* // Import json serialization
import kotlinx.serialization.json.Json // Import Json
import com.crossevol.wordbook.data.api.WordFetchApi // Import WordFetchApi
import io.ktor.client.plugins.HttpTimeout


private val logger = KotlinLogging.logger {} // Add logger instance

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kotlin-logging doesn't require explicit platform initialization like Napier
        // It typically uses the underlying platform's logging mechanism (Logcat on Android)
        logger.info { "MainActivity created." } // Log activity creation

        // Create the database driver factory
        val driverFactory = DriverFactory(this) // Pass the Android Context
        // The database instance will be created in the common App composable

        // Create the Settings instance using SharedPreferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val settings = SharedPreferencesSettings(sharedPreferences)

        // --- Configure and Create HttpClient for Android ---
        // Use the Android engine. It typically respects system proxy settings.
        val client = HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 10000 // Set request timeout to 10 seconds
            }
            // The Android engine should automatically pick up system proxy settings
            // configured in the device's network settings.
            // Explicit proxy configuration is usually not needed here.
        }
        // --- End HttpClient Configuration ---

        // Create the WordFetchApi instance with the configured client
        val wordFetchApi = WordFetchApi(client)


        setContent {
            // Pass the driver factory, settings instance, and WordFetchApi to the common App composable
            // The App composable should handle the nullable case for previews.
            App(
                settings = settings, // Pass the settings instance
                driverFactory = driverFactory, // Pass the driver factory
                wordFetchApi = wordFetchApi // Pass the WordFetchApi instance
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    // For preview, provide null or mock implementations for dependencies
    App(
        settings = null, // Null settings for preview
        driverFactory = null, // Null driver factory for preview
        wordFetchApi = null // Null API for preview
    )
}
