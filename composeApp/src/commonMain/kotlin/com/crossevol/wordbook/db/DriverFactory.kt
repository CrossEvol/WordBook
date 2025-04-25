package com.crossevol.wordbook.db

import app.cash.sqldelight.db.SqlDriver

/**
 * Expect class for creating a platform-specific SQLDelight driver.
 */
expect class DriverFactory {
    /**
     * Creates and returns a platform-specific SqlDriver.
     */
    fun createDriver(): SqlDriver
}
