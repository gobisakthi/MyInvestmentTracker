package com.personal.investmenttracker.data.repository

import com.personal.investmenttracker.data.local.dao.InterestRatePeriodDao
import com.personal.investmenttracker.data.local.entity.InterestRatePeriodEntity
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow

class InterestRateRepository(
    private val dao: InterestRatePeriodDao,
) {
    fun observeForAccount(accountId: Long): Flow<List<InterestRatePeriodEntity>> =
        dao.observeForAccount(accountId)

    suspend fun upsert(
        accountId: Long,
        effectiveYear: Int,
        effectiveMonth: Int,
        annualRateBasisPoints: Int,
        sourceNote: String?,
        existingId: Long? = null,
    ): Result<Long> {
        if (annualRateBasisPoints < 0 || annualRateBasisPoints > 5000) {
            return Result.failure(IllegalArgumentException("Rate must be between 0% and 50%."))
        }
        if (effectiveMonth !in 1..12) {
            return Result.failure(IllegalArgumentException("Invalid effective month."))
        }
        val effectiveFrom = YearMonth.of(effectiveYear, effectiveMonth).atDay(1)
        return try {
            if (existingId != null) {
                val existing = dao.getById(existingId)
                    ?: return Result.failure(NoSuchElementException("Rate period not found"))
                dao.update(
                    existing.copy(
                        annualRateBasisPoints = annualRateBasisPoints,
                        effectiveFrom = effectiveFrom,
                        sourceNote = sourceNote,
                    ),
                )
                Result.success(existingId)
            } else {
                val id = dao.insert(
                    InterestRatePeriodEntity(
                        accountId = accountId,
                        annualRateBasisPoints = annualRateBasisPoints,
                        effectiveFrom = effectiveFrom,
                        effectiveTo = null,
                        sourceNote = sourceNote,
                    ),
                )
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(id: Long): Result<Unit> {
        val row = dao.getById(id) ?: return Result.failure(NoSuchElementException("Rate period not found"))
        dao.delete(row)
        return Result.success(Unit)
    }
}
