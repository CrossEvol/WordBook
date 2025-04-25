package com.crossevol.wordbook.db

import app.cash.sqldelight.db.SqlDriver

/**
 * Helper function to create the AppDatabase instance using a DriverFactory.
 *
 * @param driverFactory The platform-specific factory to create the SqlDriver.
 * @return The created AppDatabase instance.
 */
fun createDatabase(driverFactory: DriverFactory): AppDatabase {
    val driver = driverFactory.createDriver()
    return AppDatabase(driver)
}
