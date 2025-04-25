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
        // App() // Modify App to accept database or repository
        // For now, calling App() without passing the database.
        // You'll need to update App and its dependencies to use the database.
        App()
    }
}
