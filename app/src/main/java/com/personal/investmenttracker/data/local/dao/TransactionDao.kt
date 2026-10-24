package com.personal.investmenttracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.personal.investmenttracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY effectiveDate DESC, id DESC")
    fun observeForAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Query("DELETE FROM transactions WHERE accountId = :accountId")
    suspend fun deleteAllForAccount(accountId: Long)
}
