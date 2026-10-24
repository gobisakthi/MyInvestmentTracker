package com.personal.investmenttracker.data.local

import androidx.room.migration.Migration

/**
 * Database is at version 1. Register each new [Migration] in [ALL] when the schema changes.
 * Never use destructive fallback migrations for user financial data.
 */
object Migrations {
    val ALL: Array<Migration> = emptyArray()
}
