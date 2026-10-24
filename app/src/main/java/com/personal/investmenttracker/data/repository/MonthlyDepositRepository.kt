package com.personal.investmenttracker.data.repository

import com.personal.investmenttracker.data.local.dao.MonthlyDepositDao
import com.personal.investmenttracker.data.local.entity.MonthlyDepositEntity
import kotlinx.coroutines.flow.Flow

class MonthlyDepositRepository(
    private val dao: MonthlyDepositDao,
) {
    fun observeForAccount(accountId: Long): Flow<List<MonthlyDepositEntity>> =
        dao.observeForAccount(accountId)

    suspend fun upsert(
        accountId: Long,
        year: Int,
        month: Int,
        amountMinor: Long,
        existingId: Long? = null,
    ): Result<Long> {
        if (amountMinor <= 0L) return Result.failure(IllegalArgumentException("Amount must be positive."))
        if (month !in 1..12) return Result.failure(IllegalArgumentException("Invalid month."))
        if (year !in 1900..2200) return Result.failure(IllegalArgumentException("Invalid year."))
        return try {
            val now = System.currentTimeMillis()
            if (existingId != null) {
                val existing = dao.getById(existingId)
                    ?: return Result.failure(NoSuchElementException("Deposit not found"))
                dao.update(
                    existing.copy(
                        year = year,
                        month = month,
                        amountMinor = amountMinor,
                        updatedAtEpochMillis = now,
                    ),
                )
                Result.success(existingId)
            } else {
                val id = dao.insert(
                    MonthlyDepositEntity(
                        accountId = accountId,
                        year = year,
                        month = month,
                        amountMinor = amountMinor,
                        updatedAtEpochMillis = now,
                    ),
                )
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(id: Long): Result<Unit> {
        val row = dao.getById(id) ?: return Result.failure(NoSuchElementException("Deposit not found"))
        dao.delete(row)
        return Result.success(Unit)
    }
}
