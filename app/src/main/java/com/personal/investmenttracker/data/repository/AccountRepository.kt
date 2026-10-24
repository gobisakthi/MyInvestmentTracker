package com.personal.investmenttracker.data.repository

import com.personal.investmenttracker.data.local.dao.AccountDao
import com.personal.investmenttracker.data.local.entity.AccountEntity
import com.personal.investmenttracker.domain.model.AccountSummary
import com.personal.investmenttracker.domain.model.InvestmentType
import com.personal.investmenttracker.domain.validation.ValidatedAccountForm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepository(
    private val accountDao: AccountDao,
) {
    fun observeAccountSummaries(): Flow<List<AccountSummary>> =
        accountDao.observeAll().map { accounts -> accounts.map { it.toSummary() } }

    fun observeAccount(accountId: Long): Flow<AccountEntity?> = accountDao.observeById(accountId)

    suspend fun getAccount(accountId: Long): AccountEntity? = accountDao.getById(accountId)

    suspend fun insertAccount(form: ValidatedAccountForm): Long {
        val entity = form.toNewEntity()
        return accountDao.insert(entity)
    }

    suspend fun updateAccount(accountId: Long, form: ValidatedAccountForm) {
        val existing = accountDao.getById(accountId)
            ?: throw NoSuchElementException("Account not found")
        accountDao.update(existing.withForm(form))
    }

    suspend fun deleteAccount(accountId: Long) {
        val existing = accountDao.getById(accountId)
            ?: throw NoSuchElementException("Account not found")
        accountDao.delete(existing)
    }
}

private fun AccountEntity.toSummary(): AccountSummary =
    AccountSummary(
        id = id,
        name = name,
        type = type,
        recordedBalanceMinor = openingBalanceMinor,
        balanceEffectiveDate = openingBalanceDate,
    )

private fun ValidatedAccountForm.toNewEntity(): AccountEntity =
    AccountEntity(
        name = name,
        type = type,
        openingBalanceMinor = openingBalanceMinor,
        openingBalanceDate = openingBalanceDate,
        openingBalanceIncludesCreditedInterest = openingBalanceIncludesCreditedInterest,
        createdAtEpochMillis = System.currentTimeMillis(),
        notes = notes,
        fixedAnnualRateBasisPoints = fixedAnnualRateBasisPoints,
        termEndDate = termEndDate,
        rdMonthlyAmountMinor = rdMonthlyAmountMinor,
        termStartYear = termStartYear,
        termStartMonth = termStartMonth,
        termEndYear = termEndYear,
        termEndMonth = termEndMonth,
        shareQuantity = shareQuantity,
        currentSharePriceMinor = currentSharePriceMinor,
        currentValueMinor = currentValueMinor,
    )

private fun AccountEntity.withForm(form: ValidatedAccountForm): AccountEntity =
    copy(
        name = form.name,
        type = form.type,
        openingBalanceMinor = form.openingBalanceMinor,
        openingBalanceDate = form.openingBalanceDate,
        openingBalanceIncludesCreditedInterest = form.openingBalanceIncludesCreditedInterest,
        notes = form.notes,
        fixedAnnualRateBasisPoints = form.fixedAnnualRateBasisPoints,
        termEndDate = form.termEndDate,
        rdMonthlyAmountMinor = form.rdMonthlyAmountMinor,
        termStartYear = form.termStartYear,
        termStartMonth = form.termStartMonth,
        termEndYear = form.termEndYear,
        termEndMonth = form.termEndMonth,
        shareQuantity = form.shareQuantity,
        currentSharePriceMinor = form.currentSharePriceMinor,
        currentValueMinor = form.currentValueMinor,
    )

fun List<AccountSummary>.portfolioRecordedTotalMinor(): Long =
    sumOf { it.recordedBalanceMinor }

fun List<AccountSummary>.recordedTotalFor(type: InvestmentType): Long =
    filter { it.type == type }.sumOf { it.recordedBalanceMinor }

fun List<AccountSummary>.accountCountFor(type: InvestmentType): Int =
    count { it.type == type }
