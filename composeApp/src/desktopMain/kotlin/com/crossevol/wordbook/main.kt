import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.crossevol.wordbook.App
import com.crossevol.wordbook.db.createDatabase
import com.crossevol.wordbook.db.DriverFactory
import io.github.oshai.kotlinlogging.KotlinLogging // Import KotlinLogging

private val logger = KotlinLogging.logger {} // Add logger instance

fun main() = application {
    // Kotlin-logging doesn't require explicit platform initialization like Napier
    // It typically uses the underlying platform's logging mechanism (Console on Desktop)
    logger.info { "Desktop application started." } // Log application start

    // Create the database instance using the Desktop driver factory
    val driverFactory = DriverFactory()
    val database = createDatabase(driverFactory)

    // TODO: Pass the 'database' instance or a repository using it
    // to your App composable or a dependency injection framework.
    Window(
        onCloseRequest = ::exitApplication,
        title = "WordBook",
    ) {
        // Pass the driverFactory to the App composable
        App(driverFactory = driverFactory)
    }
}
