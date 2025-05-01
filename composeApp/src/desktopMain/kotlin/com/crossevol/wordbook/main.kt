package com.crossevol.wordbook

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.crossevol.wordbook.db.DriverFactory
import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences // Import Preferences

fun main() = application {
    // Create the database driver factory for desktop
    val driverFactory = DriverFactory() // Desktop DriverFactory doesn't need context

    // Create the Settings instance using PreferencesSettings for desktop
    // PreferencesSettings requires a java.util.prefs.Preferences delegate on JVM
    val preferencesDelegate = Preferences.userRoot() // Get the user's root preferences
    val settings = PreferencesSettings(preferencesDelegate) // Pass the delegate

    Window(onCloseRequest = ::exitApplication, title = "WordBook") {
        // Pass the driver factory and settings instance to the common App composable
        App(
            settings = settings, // Pass the settings instance
            driverFactory = driverFactory // Pass the driver factory
        )
    }
}
