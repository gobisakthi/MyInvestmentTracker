package com.personal.investmenttracker.data.repository

import com.personal.investmenttracker.data.local.dao.AccountDao
import com.personal.investmenttracker.data.local.dao.InterestRatePeriodDao
import com.personal.investmenttracker.data.local.dao.MonthlyDepositDao
import com.personal.investmenttracker.data.local.entity.AccountEntity
import com.personal.investmenttracker.domain.calculation.InterestCalculationEngine
import com.personal.investmenttracker.domain.calculation.InterestCalculationResult
import com.personal.investmenttracker.domain.calculation.MonthlyInterestPoint
import com.personal.investmenttracker.domain.model.AccountSummary
import com.personal.investmenttracker.domain.model.InvestmentType
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

data class AccountWithCalculation(
    val summary: AccountSummary,
    val calculation: InterestCalculationResult,
    val monthlyBreakdown: List<MonthlyInterestPoint> = emptyList(),
)

class AccountCalculationRepository(
    private val accountDao: AccountDao,
    private val monthlyDepositDao: MonthlyDepositDao,
    private val interestRatePeriodDao: InterestRatePeriodDao,
) {
    fun observeAccountsWithCalculations(asOfDate: Flow<LocalDate>): Flow<List<AccountWithCalculation>> =
        accountDao.observeAll().flatMapLatest { accounts ->
            if (accounts.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    accounts.map { account -> observeSingleAccountCalculation(account, asOfDate) },
                ) { array -> array.toList() }
            }
        }

    fun observeAccountCalculation(accountId: Long, asOfDate: Flow<LocalDate>): Flow<AccountWithCalculation?> =
        accountDao.observeById(accountId).flatMapLatest { account ->
            if (account == null) flowOf(null) else observeSingleAccountCalculation(account, asOfDate).map { it }
        }

    private fun observeSingleAccountCalculation(
        account: AccountEntity,
        asOfDate: Flow<LocalDate>,
    ): Flow<AccountWithCalculation> {
        val depositsFlow = monthlyDepositDao.observeForAccount(account.id)
        val ratesFlow = interestRatePeriodDao.observeForAccount(account.id)
        return combine(depositsFlow, ratesFlow, asOfDate) { deposits, rates, asOf ->
            val input = account.toCalculationInput(deposits, rates)
            val result = InterestCalculationEngine.calculate(input, asOf)
            val breakdown = if (result.status == com.personal.investmenttracker.domain.calculation.CalculationStatus.OK) {
                InterestCalculationEngine.monthlyBreakdown(input, asOf)
            } else {
                emptyList()
            }
            AccountWithCalculation(
                summary = AccountSummary(
                    id = account.id,
                    name = account.name,
                    type = account.type,
                    recordedBalanceMinor = account.openingBalanceMinor,
                    balanceEffectiveDate = account.openingBalanceDate,
                ),
                calculation = result,
                monthlyBreakdown = breakdown,
            )
        }
    }
}

fun List<AccountWithCalculation>.portfolioEstimatedTotalMinor(): Long =
    filter { it.calculation.status == com.personal.investmenttracker.domain.calculation.CalculationStatus.OK }
        .sumOf { it.calculation.estimatedTotalMinor }

fun List<AccountWithCalculation>.estimatedTotalFor(type: InvestmentType): Long =
    filter { it.summary.type == type && it.calculation.status == com.personal.investmenttracker.domain.calculation.CalculationStatus.OK }
        .sumOf { it.calculation.estimatedTotalMinor }

fun List<AccountWithCalculation>.countFor(type: InvestmentType): Int =
    count { it.summary.type == type }
