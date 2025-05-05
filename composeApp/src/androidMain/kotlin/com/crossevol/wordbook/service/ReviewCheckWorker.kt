package com.crossevol.wordbook.service

import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.crossevol.wordbook.MainActivity // Import your MainActivity
import com.crossevol.wordbook.R
import com.crossevol.wordbook.data.SettingsRepository
import com.crossevol.wordbook.data.WordRepository
import com.crossevol.wordbook.db.DriverFactory
import com.crossevol.wordbook.db.createDatabase
import com.crossevol.wordbook.showNotificationWithIntent // Import the specific platform function
import com.russhwolf.settings.SharedPreferencesSettings
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val logger = KotlinLogging.logger {}

class ReviewCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "wordbookReviewCheckWorker"
        const val NAVIGATE_ACTION = "com.crossevol.wordbook.NAVIGATE_TO_REVIEW"
        const val EXTRA_DESTINATION = "destination_screen"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        logger.debug { "ReviewCheckWorker starting work..." }
        try {
            // --- Dependency Creation (Simple approach for Worker) ---
            // In a real app, use Dagger/Hilt or Koin for better DI
            val driverFactory = DriverFactory(context) // Need context for Android driver
            val database = createDatabase(driverFactory)
            val wordRepository = WordRepository(database)
            // Use SharedPreferencesSettings for Android settings persistence
            // Ensure the name matches the one used in MainActivity if sharing settings
            val settings = SharedPreferencesSettings(
                context.getSharedPreferences("wordbook_settings", Context.MODE_PRIVATE)
            )
            val settingsRepository = SettingsRepository(settings)
            // --- End Dependency Creation ---

            val reviewChecker = ReviewChecker(settingsRepository, wordRepository)

            reviewChecker.checkReviewsAndNotify { count ->
                // This lambda is called by ReviewChecker if a notification is needed
                showReviewNotification(context, count)
            }

            logger.debug { "ReviewCheckWorker finished successfully." }
            Result.success()
        } catch (e: Exception) {
            logger.error(e) { "ReviewCheckWorker failed." }
            Result.failure() // Or Result.retry() if applicable
        }
    }

    private fun showReviewNotification(context: Context, count: Int) {
        val title = context.getString(R.string.app_name) // Use app name from resources
        val message = "You have $count words ready for review!"

        // Create Intent to launch MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            action = NAVIGATE_ACTION
            putExtra(EXTRA_DESTINATION, "WordDetailSummary") // Pass target screen identifier
            // Flags to bring existing task to front or start new one
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clears back stack, adjust if needed
        }

        // Create PendingIntent
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0, // Request code
            intent,
            pendingIntentFlags
        )

        // Call the platform function that handles PendingIntent
        showNotificationWithIntent(context, title, message, pendingIntent)
    }
}
