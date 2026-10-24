package com.personal.investmenttracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.personal.investmenttracker.data.local.dao.AccountDao
import com.personal.investmenttracker.data.local.dao.InterestRatePeriodDao
import com.personal.investmenttracker.data.local.dao.TransactionDao
import com.personal.investmenttracker.data.local.entity.AccountEntity
import com.personal.investmenttracker.data.local.entity.InterestRatePeriodEntity
import com.personal.investmenttracker.data.local.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        InterestRatePeriodEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class InvestmentDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun interestRatePeriodDao(): InterestRatePeriodDao

    companion object {
        const val DATABASE_NAME = "investment_tracker.db"
    }
}
