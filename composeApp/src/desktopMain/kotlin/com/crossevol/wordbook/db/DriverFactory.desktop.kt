package com.crossevol.wordbook.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

/**
 * Actual implementation of [DriverFactory] for the Desktop (JVM) platform.
 */
actual class DriverFactory {
    /**
     * Creates a Desktop-specific SqliteDriver.
     * Uses an in-memory database for simplicity in this example.
     * For persistent storage, use a file path like "jdbc:sqlite:wordbook.db".
     */
    actual fun createDriver(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        // Create the database schema if it doesn't exist
        AppDatabase.Schema.create(driver)
        return driver
    }
}
