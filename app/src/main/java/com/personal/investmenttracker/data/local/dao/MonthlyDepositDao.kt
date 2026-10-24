package com.personal.investmenttracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.personal.investmenttracker.data.local.entity.MonthlyDepositEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyDepositDao {
    @Query(
        """
        SELECT * FROM monthly_deposits
        WHERE accountId = :accountId
        ORDER BY year DESC, month DESC
        """,
    )
    fun observeForAccount(accountId: Long): Flow<List<MonthlyDepositEntity>>

    @Query("SELECT * FROM monthly_deposits WHERE accountId = :accountId")
    suspend fun getAllForAccount(accountId: Long): List<MonthlyDepositEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(deposit: MonthlyDepositEntity): Long

    @Update
    suspend fun update(deposit: MonthlyDepositEntity)

    @Delete
    suspend fun delete(deposit: MonthlyDepositEntity)

    @Query("SELECT * FROM monthly_deposits WHERE id = :id")
    suspend fun getById(id: Long): MonthlyDepositEntity?

    @Query(
        """
        SELECT * FROM monthly_deposits
        WHERE accountId = :accountId AND year = :year AND month = :month
        LIMIT 1
        """,
    )
    suspend fun getByAccountYearMonth(accountId: Long, year: Int, month: Int): MonthlyDepositEntity?
}
