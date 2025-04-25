package com.crossevol.wordbook.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

/**
 * Actual implementation of [DriverFactory] for the Desktop (JVM) platform.
 */
actual class DriverFactory {
    /**
     * Creates a Desktop-specific SqliteDriver.
     * Uses a file-based database stored in the user's home directory.
     */
    actual fun createDriver(): SqlDriver {
        // Create directory if it doesn't exist
        val dbFolder = File(System.getProperty("user.home"), ".wordbook")
        if (!dbFolder.exists()) {
            dbFolder.mkdirs()
        }
        
        // Create the database file
        val dbFile = File(dbFolder, "wordbook.db")
        val dbPath = "jdbc:sqlite:${dbFile.absolutePath}"
        
        // Create the driver with the file path
        val driver = JdbcSqliteDriver(dbPath)
        
        // Create the database schema if it doesn't exist
        if (!dbFile.exists()) {
            AppDatabase.Schema.create(driver)
        }
        
        return driver
    }
}
