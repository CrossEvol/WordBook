package com.crossevol.wordbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.crossevol.wordbook.db.createDatabase
import com.crossevol.wordbook.db.DriverFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create the database instance using the Android driver factory
        val driverFactory = DriverFactory(this) // Pass the Android Context
        val database = createDatabase(driverFactory)

        // TODO: Pass the 'database' instance or a repository using it
        // to your App composable or a dependency injection framework.
        setContent {
            // Pass the driverFactory to the App composable
            App(driverFactory = driverFactory)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
