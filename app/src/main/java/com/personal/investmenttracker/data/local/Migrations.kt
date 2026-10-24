package com.personal.investmenttracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `monthly_deposits` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `accountId` INTEGER NOT NULL,
                    `year` INTEGER NOT NULL,
                    `month` INTEGER NOT NULL,
                    `amountMinor` INTEGER NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_monthly_deposits_accountId` ON `monthly_deposits` (`accountId`)",
            )
            db.execSQL(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS `index_monthly_deposits_accountId_year_month`
                ON `monthly_deposits` (`accountId`, `year`, `month`)
                """.trimIndent(),
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2)
}
