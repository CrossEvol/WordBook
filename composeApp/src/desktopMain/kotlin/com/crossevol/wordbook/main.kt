import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.crossevol.wordbook.App
import com.crossevol.wordbook.db.DriverFactory
import com.russhwolf.settings.PreferencesSettings // Import PreferencesSettings
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {} // Add logger instance

fun main() = application {
    // Kotlin-logging doesn't require explicit platform initialization like Napier
    // It typically uses the underlying platform's logging mechanism (Console on Desktop)
    logger.info { "Desktop application started." } // Log application start

    // Create the database instance using the Desktop driver factory
    // The database instance will be created in the common App composable
    val driverFactory = DriverFactory()

    // Create the Settings instance using Preferences
    val settings = PreferencesSettings.Factory().create("wordbook_settings") // Use a named preference set


    Window(onCloseRequest = ::exitApplication, title = "Wordbook") {
        // Pass the driver factory and settings instance to the common App composable
        // The App composable should handle the nullable case for previews.
        App(
            settings = settings, // Pass the settings instance
            driverFactory = driverFactory // Pass the driver factory
        )
    }
}
