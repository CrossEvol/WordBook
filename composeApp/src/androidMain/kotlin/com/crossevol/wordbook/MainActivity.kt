package com.crossevol.wordbook

import android.content.Intent
import android.os.Bundle
// import android.preference.PreferenceManager // Not needed if using named SharedPreferences
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.* // Import compose runtime
import androidx.work.* // Import WorkManager
import com.crossevol.wordbook.db.DriverFactory
import com.crossevol.wordbook.service.ReviewCheckWorker // Import worker
import io.github.oshai.kotlinlogging.KotlinLogging
// Remove org.jetbrains.compose.ui.tooling.preview.Preview if AppAndroidPreview is removed or uses androidx preview
import com.russhwolf.settings.SharedPreferencesSettings // Import SharedPreferencesSettings
import io.ktor.client.* // Import HttpClient
import io.ktor.client.engine.android.* // Import Android engine
import io.ktor.client.plugins.contentnegotiation.* // Import ContentNegotiation
import io.ktor.serialization.kotlinx.json.* // Import json serialization
import kotlinx.serialization.json.Json // Import Json
import com.crossevol.wordbook.data.api.WordFetchApi // Import WordFetchApi
import io.ktor.client.plugins.HttpTimeout
import java.util.concurrent.TimeUnit // Import TimeUnit


private val logger = KotlinLogging.logger {} // Add logger instance

class MainActivity : ComponentActivity() {

    // State to hold the initial screen destination from intent
    private var initialDestination by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.info { "MainActivity onCreate" }

        // Initialize the platform extension with the application context
        initPlatformExt(this)

        // Kotlin-logging doesn't require explicit platform initialization like Napier
        // It typically uses the underlying platform's logging mechanism (Logcat on Android)
        // --- Dependency Setup ---
        // Use a specific name for settings SharedPreferences
        val settings = SharedPreferencesSettings(
            getSharedPreferences(
                "wordbook_settings",
                MODE_PRIVATE
            )
        )
        val driverFactory = DriverFactory(this)

        // Setup Ktor HTTP Client (Android specific part)
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

        val wordFetchApi = WordFetchApi(client)
        // --- End Dependency Setup ---

        // Handle intent that started the activity (e.g., from notification)
        handleIntent(intent)

        // Schedule the background worker
        scheduleReviewWorker()

        setContent {
            // Remember the initial destination to avoid recomposition issues
            val rememberedInitialDestination = remember { initialDestination }

            // Call the refactored App composable
            App(
                driverFactory = driverFactory,
                settings = settings,
                wordFetchApi = wordFetchApi,
                initialScreenRoute = rememberedInitialDestination // Pass route string
                // desktopReviewTrigger and onDesktopReviewTriggerHandled are null/default for Android
            )
        }
    }

   override fun onNewIntent(intent: Intent) {
       super.onNewIntent(intent)
        logger.info { "MainActivity onNewIntent" }
        // Handle intent if the activity is already running and receives a new intent
        handleIntent(intent)
        // If App uses the initialScreen parameter, setting initialDestination here
        // might require recomposing App or using a different signaling mechanism (e.g., Flow).
        // For simplicity with initialScreen parameter, we might not need to do much here
        // unless the App needs to react *dynamically* to new intents while running.
        // If dynamic reaction is needed, consider using a SharedFlow/StateFlow in a ViewModel
        // observed by both MainActivity and App.
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == ReviewCheckWorker.NAVIGATE_ACTION) {
            val destination = intent.getStringExtra(ReviewCheckWorker.EXTRA_DESTINATION)
            // Update the state that App observes or uses for initial composition
            initialDestination = destination
            logger.info { "Intent received with action ${intent.action}, destination: $initialDestination" }
            // Clear the intent action to prevent re-processing on configuration change if needed
            // intent.action = null // Be careful if other parts rely on the action persisting
        } else {
            // Don't reset initialDestination here if App uses it only on initial composition.
            // If it's a normal launch, initialDestination will be null by default.
            logger.debug { "Intent received with action ${intent?.action}. Not a review navigation." }
        }
    }


    private fun scheduleReviewWorker() {
        // Use PeriodicWorkRequest to run roughly every 15 minutes (minimum interval)
        // WorkManager optimizes execution based on battery, network etc.
        val constraints = Constraints.Builder()
            // Add constraints if needed, e.g., require network, battery not low
            // .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<ReviewCheckWorker>(
            repeatInterval = 15, // Minimum interval for periodic work
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            // Set backoff policy if needed
            // .setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()

        // Enqueue the work uniquely to avoid duplicates
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ReviewCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if already scheduled
            // Use REPLACE if you want changes in the worker/constraints to take effect immediately
            periodicWorkRequest
        )
        logger.info { "Scheduled periodic review worker (${ReviewCheckWorker.WORK_NAME})." }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Optional: Cancel work if needed, but usually periodic work should persist
        // WorkManager.getInstance(this).cancelUniqueWork(ReviewCheckWorker.WORK_NAME)
        logger.info { "MainActivity onDestroy" }
    }
}

// Preview might need adjustment if dependencies changed significantly
// Or remove if not needed / causing issues
/*
// Preview needs significant changes or removal due to internal state management in App
@Preview
@Composable
fun AppAndroidPreview() {
    // Need mock DriverFactory, Settings, WordFetchApi for preview
    // val mockDriverFactory = ...
    // val mockSettings = ...
    // val mockWordFetchApi = ...
    // App(
    //     driverFactory = mockDriverFactory,
    //     settings = mockSettings,
    //     wordFetchApi = mockWordFetchApi
    // )
}
*/
