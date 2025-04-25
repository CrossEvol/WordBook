package com.crossevol.wordbook.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Repository for managing application settings using multiplatform-settings.
 */
class SettingsRepository(private val settings: Settings) {

    private val LOCALE_KEY = "app_locale"
    private val DEFAULT_LOCALE = "EN" // Default locale

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
}
