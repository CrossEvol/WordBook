package com.crossevol.wordbook.service

import com.crossevol.wordbook.data.SettingsRepository
import com.crossevol.wordbook.data.WordRepository
import com.crossevol.wordbook.showNotification // Import the common showNotification
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger {}

class DesktopReviewScheduler(
    private val settingsRepository: SettingsRepository,
    private val wordRepository: WordRepository,
    private val coroutineScope: CoroutineScope, // Scope tied to application lifecycle
    private val onNotificationClick: () -> Unit // Callback to trigger navigation
) {
    private var job: Job? = null
    private val checkInterval = 1.minutes // Check more frequently on desktop when app is running

    fun start() {
        if (job?.isActive == true) {
            logger.debug { "DesktopReviewScheduler already running." }
            return
        }
        logger.info { "Starting DesktopReviewScheduler..." }
        val reviewChecker = ReviewChecker(settingsRepository, wordRepository)

        job = coroutineScope.launch(Dispatchers.Default) { // Use Default dispatcher for checks
            while (isActive) {
                try {
                    logger.trace { "DesktopReviewScheduler checking..." }
                    reviewChecker.checkReviewsAndNotify { count ->
                        // Construct title and message based on the count
                        val title = "Review Reminder"
                        val message = if (count > 0) {
                            "You have $count words due for review!"
                        } else {
                            "No words due for review right now."
                        }
                        // Call the common showNotification, passing the desktop-specific click handler
                        showNotification(title, message, onNotificationClick)
                    }
                } catch (e: CancellationException) {
                    logger.info { "DesktopReviewScheduler job cancelled." }
                    throw e // Re-throw cancellation
                } catch (e: Exception) {
                    logger.error(e) { "Error during desktop review check." }
                    // Avoid crashing the loop, maybe add longer delay on error
                }
                delay(checkInterval)
            }
        }
    }

    fun stop() {
        logger.info { "Stopping DesktopReviewScheduler..." }
        job?.cancel()
        job = null
    }
}
