package com.crossevol.wordbook

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.crossevol.wordbook.db.DriverFactory
import com.crossevol.wordbook.db.createDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.russhwolf.settings.SharedPreferencesSettings // Import SharedPreferencesSettings

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

        setContent {
            // Pass the driver factory and settings instance to the common App composable
            // The App composable should handle the nullable case for previews.
            App(
                settings = settings, // Pass the settings instance
                driverFactory = driverFactory // Pass the driver factory
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
