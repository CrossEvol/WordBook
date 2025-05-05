package com.crossevol.wordbook.service

import com.crossevol.wordbook.data.NotificationFrequency
import com.crossevol.wordbook.data.SettingsRepository
import com.crossevol.wordbook.data.WordRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.* // Use kotlinx-datetime for time manipulation
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger {}

class ReviewChecker(
    private val settingsRepository: SettingsRepository,
    private val wordRepository: WordRepository
) {

    // Keep track of the last notification time for each frequency to avoid spamming
    private val lastNotificationTime = mutableMapOf<NotificationFrequency, Instant>()
    // Note: checkInterval is not used within this class directly, but determines how often the platform scheduler runs this check.

    /**
     * Checks all enabled notification frequencies and triggers the callback if reviews are due
     * and the schedule criteria are met.
     *
     * @param showNotificationCallback A function to call when a notification should be shown.
     *                                 It receives the count of words needing review.
     */
    suspend fun checkReviewsAndNotify(showNotificationCallback: (count: Int) -> Unit) {
        if (!settingsRepository.getNotificationPermissionEnabled()) {
            // logger.debug { "Notification permission not enabled in settings. Skipping check." }
            return // Don't check if user disabled notifications globally
        }

        val now = Clock.System.now()
        val currentLocalDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val currentLocale = settingsRepository.getLocale() // Assuming locale affects language preference

        var wordsDueCount = -1 // Cache the count to avoid multiple DB queries

        for (frequency in NotificationFrequency.entries) {
            if (settingsRepository.isNotificationFrequencyEnabled(frequency)) {
                val startTimeStr = settingsRepository.getNotificationFrequencyStartTime(frequency)
                val (startHour, startMinute) = parseTime(startTimeStr)

                if (shouldCheckNow(frequency, startHour, startMinute, now, currentLocalDateTime)) {
                    // Avoid re-notifying too quickly for the same frequency
                    val lastNotified = lastNotificationTime[frequency]
                    val cooldown = getCooldownPeriod(frequency) // Prevent spamming
                    if (lastNotified != null && (now - lastNotified) < cooldown) {
                         logger.debug { "Cooldown active for $frequency. Last notified: $lastNotified. Skipping." }
                        continue
                    }

                    // Lazily query the database only when needed
                    if (wordsDueCount == -1) {
                        wordsDueCount = try {
                            // Use Default dispatcher for potentially blocking DB access
                            // Although SQLDelight drivers might handle threading, it's safer.
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                                wordRepository.getWordsNeedingReview(currentLocale).size
                            }
                        } catch (e: Exception) {
                            logger.error(e) { "Error getting words needing review for locale $currentLocale" }
                            0 // Assume 0 on error
                        }
                    }

                    if (wordsDueCount > 0) {
                        logger.info { "Review check for $frequency: $wordsDueCount words due. Triggering notification." }
                        showNotificationCallback(wordsDueCount)
                        lastNotificationTime[frequency] = now // Update last notification time
                        // Important: Break or continue based on whether you want multiple notifications
                        // if multiple schedules match simultaneously. Usually, one is enough.
                        break
                    } else {
                         logger.debug { "Review check for $frequency: No words due." }
                         // Update time even if 0 words due, to prevent immediate re-check after cooldown
                         // Only update if the schedule *matched*, even if count is 0
                         lastNotificationTime[frequency] = now
                    }
                }
            }
        }
    }

    private fun parseTime(timeStr: String): Pair<Int, Int> {
        return try {
            val parts = timeStr.split(":")
            Pair(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) {
            logger.warn { "Failed to parse time string '$timeStr', defaulting to 09:00." }
            Pair(9, 0) // Default time
        }
    }

    private fun getCooldownPeriod(frequency: NotificationFrequency): Duration {
        // Set a cooldown slightly less than the frequency to avoid missing checks
        // but prevent rapid re-notifications if the check runs often.
        return when (frequency) {
            NotificationFrequency.MINUTES_15 -> 14.minutes
            NotificationFrequency.HOURLY -> 59.minutes
            NotificationFrequency.DAILY -> 23.hours + 55.minutes
            NotificationFrequency.WEEKLY -> 6.days + 23.hours
            NotificationFrequency.MONTHLY -> 27.days // Approximate
        }
    }

    private fun shouldCheckNow(
        frequency: NotificationFrequency,
        startHour: Int,
        startMinute: Int,
        now: Instant,
        currentLocalDateTime: LocalDateTime
    ): Boolean {
        val scheduledTimeToday = try {
            currentLocalDateTime.date.atTime(startHour, startMinute)
        } catch (e: IllegalArgumentException) {
             logger.warn { "Invalid start time configuration ($startHour:$startMinute) for $frequency. Skipping." }
             return false // Invalid time components
        }

        // Convert scheduled time to Instant for comparison
        val scheduledInstantToday = scheduledTimeToday.toInstant(TimeZone.currentSystemDefault())

        // Check if the scheduled time has passed today
        val scheduledTimePassed = now >= scheduledInstantToday

        // Check if we already notified for this period based on lastNotificationTime
        val lastNotified = lastNotificationTime[frequency]
        val alreadyNotifiedThisPeriod = if (lastNotified != null) {
            when (frequency) {
                // For frequent checks, rely more on the cooldown handled in the main loop
                NotificationFrequency.MINUTES_15, NotificationFrequency.HOURLY -> false // Cooldown handles this
                // For daily/weekly/monthly, check if the last notification was within the current period
                NotificationFrequency.DAILY -> lastNotified.toLocalDateTime(TimeZone.currentSystemDefault()).date == currentLocalDateTime.date
                NotificationFrequency.WEEKLY -> {
                    val lastNotifiedDate = lastNotified.toLocalDateTime(TimeZone.currentSystemDefault()).date
                    // Check if same week (approximation: within last 6 days and same day of week or earlier)
                    // More robust: Check if 'now' is in a different week than 'lastNotified' based on a specific start day (e.g., Monday)
                    val daysSinceLast = now.minus(lastNotified).inWholeDays
                    daysSinceLast >= 7 || // More than a week ago
                    (lastNotifiedDate.dayOfWeek.ordinal < scheduledTimeToday.dayOfWeek.ordinal && daysSinceLast < 7) // Same week, but before scheduled day
                }
                NotificationFrequency.MONTHLY -> {
                     val lastNotifiedDate = lastNotified.toLocalDateTime(TimeZone.currentSystemDefault()).date
                     lastNotifiedDate.year != currentLocalDateTime.year || lastNotifiedDate.month != currentLocalDateTime.month
                }
            }
        } else false


        if (!scheduledTimePassed || alreadyNotifiedThisPeriod) {
            // logger.trace { "Skipping check for $frequency: Scheduled time passed: $scheduledTimePassed, Already notified this period: $alreadyNotifiedThisPeriod" }
            return false
        }

        // Specific frequency logic - only check day constraints for less frequent schedules
        return when (frequency) {
            NotificationFrequency.MINUTES_15 -> true // Always check if enabled and cooldown passed
            NotificationFrequency.HOURLY -> true // Always check if enabled and cooldown passed
            NotificationFrequency.DAILY -> true // Check daily after start time
            NotificationFrequency.WEEKLY -> currentLocalDateTime.dayOfWeek == DayOfWeek.SUNDAY // Example: Check on Sundays after start time
            NotificationFrequency.MONTHLY -> currentLocalDateTime.dayOfMonth == 1 // Example: Check on the 1st of the month after start time
        }
    }
}
