package com.personal.investmenttracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.personal.investmenttracker.data.local.entity.InterestRatePeriodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InterestRatePeriodDao {
    @Query(
        """
        SELECT * FROM interest_rate_periods
        WHERE accountId = :accountId
        ORDER BY effectiveFrom ASC, id ASC
        """,
    )
    fun observeForAccount(accountId: Long): Flow<List<InterestRatePeriodEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(period: InterestRatePeriodEntity): Long

    @Update
    suspend fun update(period: InterestRatePeriodEntity)

    @Delete
    suspend fun delete(period: InterestRatePeriodEntity)

    @Query("SELECT * FROM interest_rate_periods WHERE id = :id")
    suspend fun getById(id: Long): InterestRatePeriodEntity?
}
