package com.crossevol.wordbook

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf // Import derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.crossevol.wordbook.data.ApiKeyConfigRepository
import com.crossevol.wordbook.data.SettingsRepository
import com.crossevol.wordbook.data.WordRepository
import com.crossevol.wordbook.data.api.WordFetchApi
import com.crossevol.wordbook.db.DriverFactory
import com.crossevol.wordbook.db.createDatabase
import com.crossevol.wordbook.service.DesktopReviewScheduler
import com.crossevol.wordbook.ui.viewmodel.ApiKeyViewModel
import com.crossevol.wordbook.ui.viewmodel.WordFetchViewModel
import com.crossevol.wordbook.ui.viewmodel.WordReviewViewModel
import com.russhwolf.settings.PreferencesSettings
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import kotlinx.serialization.json.Json
import java.net.URI
import java.util.prefs.Preferences

private val logger = KotlinLogging.logger {} // Add logger instance

fun main() { // Changed from = application { ... } to { application { ... } }
    application {
        // Create a CoroutineScope for the application lifecycle
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        // --- Dependency Setup (Core only) ---
        val driverFactory = DriverFactory()
        // Database, Repositories, SettingsRepository, ViewModels will be created inside App
        val preferencesDelegate = Preferences.userRoot().node("com.crossevol.wordbook")
        val settings = PreferencesSettings(preferencesDelegate) // Settings needed by App

        // Setup Ktor HTTP Client (Desktop specific part) - Needed by App
        // Attempt to read proxy settings from environment variables (common for CLI/Desktop)
        // Note: Setting JVM system properties is a common way for Java/Kotlin apps
        // to pick up proxy settings, especially when using engines like OkHttp
        // which often delegate to the JVM's default ProxySelector.
        val httpProxy = System.getenv("http_proxy") ?: System.getenv("HTTP_PROXY")
        val httpsProxy = System.getenv("https_proxy") ?: System.getenv("HTTPS_PROXY")

        // Set JVM system properties for proxy if environment variables are found
        if (!httpProxy.isNullOrBlank()) {
            try {
                val uri = URI(httpProxy)
                System.setProperty(
                    "http.proxyHost",
                    uri.host
                )
                System.setProperty(
                    "http.proxyPort",
                    uri.port.toString()
                )
                // Note: Handling proxy authentication (user:pass@host:port) is more complex
                // and might require configuring OkHttp's Authenticator.
                // For now, we rely on basic host/port.
                System.setProperty(
                    "java.net.useSystemProxies",
                    "true"
                ) // Encourage using system/JVM properties
                println("Using HTTP proxy from environment: $httpProxy") // Log for debugging
            } catch (e: Exception) {
                println("Warning: Could not parse http_proxy environment variable: $httpProxy. Error: ${e.message}")
            }
        }

        if (!httpsProxy.isNullOrBlank()) {
            try {
                val uri = URI(httpsProxy)
                System.setProperty(
                    "https.proxyHost",
                    uri.host
                )
                System.setProperty(
                    "https.proxyPort",
                    uri.port.toString()
                )
                // Note: Handling proxy authentication is more complex
                System.setProperty(
                    "java.net.useSystemProxies",
                    "true"
                ) // Encourage using system/JVM properties
                println("Using HTTPS proxy from environment: $httpsProxy") // Log for debugging
            } catch (e: Exception) {
                println("Warning: Could not parse https_proxy environment variable: $httpsProxy. Error: ${e.message}")
            }
        }

        // Create the HttpClient instance using the OkHttp engine
        val client = HttpClient(OkHttp) {
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
            // OkHttp engine should pick up JVM system properties set above
            // or the system's default ProxySelector if java.net.useSystemProxies is true.
            // This allows it to potentially use system-wide proxy settings or environment variables.
            // Explicit OkHttp proxy configuration would go here if needed, e.g.,
            // engine {
            //     config {
            //         // Example of explicit proxy config (usually not needed if system properties work)
            //         // proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("your_proxy_host", 8080))
            //     }
            // } // Removed the extra closing brace here
        }
        val wordFetchApi = WordFetchApi(client) // WordFetchApi needed by App
        // --- End Dependency Setup ---


        // --- Navigation State Trigger (for Desktop Notifications) ---
        var navigateToReview by remember { mutableStateOf(false) }
        // currentScreenState is now managed inside App

        // --- Scheduler Setup ---
        // Need WordRepository and SettingsRepository for the scheduler.
        // Since they are created inside App, we either need to:
        // A) Create them here *again* just for the scheduler (inefficient)
        // B) Pass DriverFactory/Settings to scheduler and let it create its own instances
        // C) Hoist repository creation *outside* App again (defeats part of the goal)
        // D) Pass a callback from App *up* to the scheduler (complex)
        // Let's choose B for now, accepting the duplication for looser coupling.
        // Alternatively, if scheduler only needs *settings*, pass only settingsRepository.
        // Let's assume scheduler needs both for now.
        val schedulerWordRepository = WordRepository(createDatabase(driverFactory)) // Create instance for scheduler
        val schedulerSettingsRepository = SettingsRepository(settings) // Create instance for scheduler

        val scheduler = DesktopReviewScheduler(
            settingsRepository = schedulerSettingsRepository, // Pass scheduler's instance
            wordRepository = schedulerWordRepository,       // Pass scheduler's instance
            coroutineScope = applicationScope,
            onNotificationClick = {
                // This callback is executed when the notification is clicked
                // Update the UI state, ensuring it runs on the UI thread (Swing)
                applicationScope.launch(Dispatchers.Swing) {
                    logger.info { "Notification click handler: Requesting navigation to review." }
                    navigateToReview = true
                }
            }
        )

        // Start the scheduler when the application launches
        LaunchedEffect(Unit) {
            scheduler.start()
        }
        // --- End Scheduler Setup ---

        // Navigation trigger effect is now inside App

        val windowState = rememberWindowState(
            size = DpSize(
                600.dp,
                800.dp
            ) // Set desired initial size
        )
        val icon: Painter = painterResource("word_book_icon.png")


        Window(
            onCloseRequest = {
                logger.info { "Window close requested. Stopping scheduler and exiting." }
                // Cancel the scope when the window closes
                scheduler.stop() // Explicitly stop scheduler if needed
                applicationScope.cancel("Application closing")
                exitApplication()
            },
            title = "WordBook",
            icon = icon, // Use the potentially null icon
            state = windowState
        ) {
            // Call the refactored App composable
            App(
                driverFactory = driverFactory,
                settings = settings,
                wordFetchApi = wordFetchApi,
                // initialScreenRoute is null/default for Desktop
                desktopReviewTrigger = remember { derivedStateOf { navigateToReview } }, // Pass state trigger
                onDesktopReviewTriggerHandled = { navigateToReview = false } // Pass handler callback
            )
        }

        // Ensure the scope is cancelled on exit (redundant with onCloseRequest but safe)
        Runtime.getRuntime().addShutdownHook(Thread {
            logger.info { "Shutdown hook triggered. Cancelling application scope." }
            applicationScope.cancel("Shutdown hook")
        })
    } // Closing brace for the application block
} // Closing brace for the main function


