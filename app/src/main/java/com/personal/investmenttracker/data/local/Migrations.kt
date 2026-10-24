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

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `accounts` ADD COLUMN `fixedAnnualRateBasisPoints` INTEGER")
            db.execSQL("ALTER TABLE `accounts` ADD COLUMN `termEndDate` TEXT")
            db.execSQL("ALTER TABLE `accounts` ADD COLUMN `rdMonthlyAmountMinor` INTEGER")
            db.execSQL("ALTER TABLE `accounts` ADD COLUMN `termStartYear` INTEGER")
            db.execSQL("ALTER TABLE `accounts` ADD COLUMN `termStartMonth` INTEGER")
            db.execSQL("ALTER TABLE `accounts` ADD COLUMN `termEndYear` INTEGER")
            db.execSQL("ALTER TABLE `accounts` ADD COLUMN `termEndMonth` INTEGER")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `accounts` ADD COLUMN `shareQuantity` REAL")
            db.execSQL("ALTER TABLE `accounts` ADD COLUMN `currentSharePriceMinor` INTEGER")
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `accounts` ADD COLUMN `currentValueMinor` INTEGER")
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
}
