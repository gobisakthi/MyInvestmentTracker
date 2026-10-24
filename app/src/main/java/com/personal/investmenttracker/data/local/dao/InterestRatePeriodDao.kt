package com.personal.investmenttracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
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

    @Insert
    suspend fun insert(period: InterestRatePeriodEntity): Long

    @Query("DELETE FROM interest_rate_periods WHERE accountId = :accountId")
    suspend fun deleteAllForAccount(accountId: Long)
}
