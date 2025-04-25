package com.crossevol.wordbook.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Actual implementation of [DriverFactory] for the Android platform.
 */
actual class DriverFactory(private val context: Context) {
    /**
     * Creates an Android-specific SqliteDriver.
     * The database file will be named "wordbook.db".
     */
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(AppDatabase.Schema, context, "wordbook.db")
    }
}
