package com.crossevol.wordbook.data

import com.russhwolf.settings.Settings
import androidx.compose.ui.graphics.Color // Import Color
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Represents the different frequency options for notifications.
 */
enum class NotificationFrequency(
    val keySuffix: String,
    val displayName: String,
    val color: Color // Add color property
) {
    MINUTES_15(
        "15m",
        "15 Mins", // Shorter name
        Color(0xFF00BCD4) // Cyan 500
    ),
    HOURLY(
        "1h",
        "Hourly", // Shorter name
        Color(0xFF03A9F4) // Light Blue 500
    ),
    DAILY(
        "1d",
        "Daily", // Shorter name
        Color(0xFF4CAF50) // Green 500
    ),
    WEEKLY(
        "1w",
        "Weekly", // Shorter name
        Color(0xFFFF9800) // Orange 500
    ),
    MONTHLY(
        "1mo",
        "Monthly", // Shorter name
        Color(0xFF9C27B0) // Purple 500
    )
}

/**
 * Repository for managing application settings using multiplatform-settings.
 */
class SettingsRepository(private val settings: Settings) {

    private val LOCALE_KEY = "app_locale"
    private val DEFAULT_LOCALE = "EN" // Default locale

    // Notification Settings Keys
    private val NOTIFICATION_PERMISSION_KEY = "notification_permission_enabled"
    private val NOTIFICATION_FREQUENCY_ENABLED_PREFIX = "notification_freq_enabled_"
    private val NOTIFICATION_FREQUENCY_START_TIME_PREFIX = "notification_freq_start_time_"
    private val DEFAULT_START_TIME = "09:00" // Default start time HH:mm

    /**
     * Gets the currently saved locale.
     * Returns the default locale if no locale is saved.
     */
    fun getLocale(): String {
        val locale = settings[LOCALE_KEY, DEFAULT_LOCALE]
        logger.debug { "Retrieved locale: $locale" }
        return locale
    }

    /**
     * Sets the application locale.
     */
    fun setLocale(locale: String) {
        settings[LOCALE_KEY] = locale
        logger.info { "Saved locale: $locale" }
    }

    // --- Notification Settings ---

    /** Gets whether notification permission is granted (user setting). Defaults to true. */
    fun getNotificationPermissionEnabled(): Boolean {
        val enabled = settings[NOTIFICATION_PERMISSION_KEY, true]
        logger.debug { "Retrieved notification permission enabled: $enabled" }
        return enabled
    }

    /** Sets whether notification permission is granted (user setting). */
    fun setNotificationPermissionEnabled(enabled: Boolean) {
        settings[NOTIFICATION_PERMISSION_KEY] = enabled
        logger.info { "Saved notification permission enabled: $enabled" }
    }

    /** Checks if a specific notification frequency is enabled. Defaults to false. */
    fun isNotificationFrequencyEnabled(frequency: NotificationFrequency): Boolean {
        val key = NOTIFICATION_FREQUENCY_ENABLED_PREFIX + frequency.keySuffix
        val enabled = settings[key, false] // Default to false
        logger.debug { "Retrieved frequency enabled for ${frequency.name}: $enabled" }
        return enabled
    }

    /** Sets whether a specific notification frequency is enabled. */
    fun setNotificationFrequencyEnabled(
        frequency: NotificationFrequency,
        enabled: Boolean
    ) {
        val key = NOTIFICATION_FREQUENCY_ENABLED_PREFIX + frequency.keySuffix
        settings[key] = enabled
        logger.info { "Saved frequency enabled for ${frequency.name}: $enabled" }
    }

    /** Gets the start time (HH:mm format) for a specific notification frequency. Defaults to "09:00". */
    fun getNotificationFrequencyStartTime(frequency: NotificationFrequency): String {
        val key = NOTIFICATION_FREQUENCY_START_TIME_PREFIX + frequency.keySuffix
        val time = settings[key, DEFAULT_START_TIME]
        logger.debug { "Retrieved frequency start time for ${frequency.name}: $time" }
        return time
    }

    /** Sets the start time (HH:mm format) for a specific notification frequency. */
    fun setNotificationFrequencyStartTime(
        frequency: NotificationFrequency,
        time: String
    ) {
        val key = NOTIFICATION_FREQUENCY_START_TIME_PREFIX + frequency.keySuffix
        settings[key] = time
        logger.info { "Saved frequency start time for ${frequency.name}: $time" }
    }
}
