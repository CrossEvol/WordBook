import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.crossevol.wordbook.App
import com.crossevol.wordbook.db.createDatabase
import com.crossevol.wordbook.db.DriverFactory

fun main() = application {
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
