package com.personal.investmenttracker.data.repository

import com.personal.investmenttracker.data.local.dao.AccountDao
import com.personal.investmenttracker.data.local.dao.InterestRatePeriodDao
import com.personal.investmenttracker.data.local.dao.MonthlyDepositDao
import com.personal.investmenttracker.data.local.entity.AccountEntity
import com.personal.investmenttracker.domain.calculation.CalculationStatus
import com.personal.investmenttracker.domain.calculation.InterestCalculationEngine
import com.personal.investmenttracker.domain.calculation.InterestCalculationResult
import com.personal.investmenttracker.domain.calculation.MonthlyInterestPoint
import com.personal.investmenttracker.domain.calculation.ManualValueHoldingsCalculation
import com.personal.investmenttracker.domain.calculation.StockHoldingsCalculation
import com.personal.investmenttracker.domain.calculation.TermDepositCalculations
import com.personal.investmenttracker.domain.model.AccountSummary
import com.personal.investmenttracker.domain.model.InvestmentType
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

data class AccountWithCalculation(
    val summary: AccountSummary,
    val calculation: InterestCalculationResult,
    val monthlyBreakdown: List<MonthlyInterestPoint> = emptyList(),
    val shareQuantity: Double? = null,
    val currentSharePriceMinor: Long? = null,
    val currentValueMinor: Long? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
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
        if (account.type == InvestmentType.FD) {
            return asOfDate.map { asOf -> account.toFixedDepositEvaluation(asOf) }
        }
        if (account.type == InvestmentType.RD) {
            return asOfDate.map { asOf -> account.toRecurringDepositEvaluation(asOf) }
        }
        if (account.type.usesQuantityAndPrice() || account.type.usesManualCurrentValue()) {
            return accountDao.observeById(account.id).flatMapLatest { entity ->
                val row = entity ?: account
                flowOf(row.toSimpleHoldingsEvaluation())
            }
        }
        val depositsFlow = monthlyDepositDao.observeForAccount(account.id)
        val ratesFlow = interestRatePeriodDao.observeForAccount(account.id)
        return combine(depositsFlow, ratesFlow, asOfDate) { deposits, rates, asOf ->
            val input = account.toCalculationInput(deposits, rates)
            val result = InterestCalculationEngine.calculate(input, asOf)
            val breakdown = if (result.status == CalculationStatus.OK) {
                InterestCalculationEngine.monthlyBreakdown(input, asOf)
            } else {
                emptyList()
            }
            account.toAccountWithCalculation(result, breakdown)
        }
    }

    private fun AccountEntity.toFixedDepositEvaluation(asOf: LocalDate): AccountWithCalculation {
        val terms = toFixedDepositTerms()
        val input = terms?.let { TermDepositCalculations.buildFixedDepositInput(it) }
        val result = when {
            input == null -> InterestCalculationResult(
                status = CalculationStatus.INVALID_INPUT,
                messages = listOf("FD is missing term or rate details."),
            )
            else -> InterestCalculationEngine.calculate(input, asOf)
        }
        val breakdown = if (input != null && result.status == CalculationStatus.OK) {
            InterestCalculationEngine.monthlyBreakdown(input, asOf)
        } else {
            emptyList()
        }
        return toAccountWithCalculation(result, breakdown)
    }

    private fun AccountEntity.toRecurringDepositEvaluation(asOf: LocalDate): AccountWithCalculation {
        val terms = toRecurringDepositTerms()
        val input = terms?.let { TermDepositCalculations.buildRecurringDepositInput(it) }
        val result = when {
            input == null -> InterestCalculationResult(
                status = CalculationStatus.INVALID_INPUT,
                messages = listOf("RD is missing term or rate details."),
            )
            else -> InterestCalculationEngine.calculate(input, asOf)
        }
        val breakdown = if (input != null && result.status == CalculationStatus.OK) {
            InterestCalculationEngine.monthlyBreakdown(input, asOf)
        } else {
            emptyList()
        }
        return toAccountWithCalculation(result, breakdown)
    }

    private fun AccountEntity.toSimpleHoldingsEvaluation(): AccountWithCalculation {
        val result = when {
            type.usesQuantityAndPrice() ->
                StockHoldingsCalculation.calculate(shareQuantity, currentSharePriceMinor)
            type.usesManualCurrentValue() ->
                ManualValueHoldingsCalculation.calculate(currentValueMinor)
            else -> InterestCalculationResult(
                status = CalculationStatus.INVALID_INPUT,
                messages = listOf("Unsupported holding type."),
            )
        }
        return toAccountWithCalculation(result, emptyList())
    }

    private fun AccountEntity.toAccountWithCalculation(
        result: InterestCalculationResult,
        breakdown: List<MonthlyInterestPoint>,
    ): AccountWithCalculation =
        AccountWithCalculation(
            summary = AccountSummary(
                id = id,
                name = name,
                type = type,
                recordedBalanceMinor = result.estimatedTotalMinor.takeIf { result.status == CalculationStatus.OK }
                    ?: openingBalanceMinor,
                balanceEffectiveDate = openingBalanceDate,
            ),
            calculation = result,
            monthlyBreakdown = breakdown,
            shareQuantity = shareQuantity,
            currentSharePriceMinor = currentSharePriceMinor,
            currentValueMinor = currentValueMinor,
        )
}

fun List<AccountWithCalculation>.portfolioEstimatedTotalMinor(
    includeTypes: Set<InvestmentType>? = null,
): Long =
    filter { includeTypes == null || it.summary.type in includeTypes }
        .filter { it.calculation.status == CalculationStatus.OK }
        .sumOf { it.calculation.estimatedTotalMinor }

fun List<AccountWithCalculation>.portfolioAccruedInterestMinor(
    includeTypes: Set<InvestmentType>? = null,
): Long =
    filter { includeTypes == null || it.summary.type in includeTypes }
        .filter { it.summary.type.usesInterestAccrual() && it.calculation.status == CalculationStatus.OK }
        .sumOf { it.calculation.accruedInterestMinor() }

fun List<AccountWithCalculation>.portfolioEstimatedDailyInterestMinor(
    includeTypes: Set<InvestmentType>? = null,
): Long =
    dailyInterestBreakdown(includeTypes).sumOf { it.dailyInterestMinor }

data class AccountDailyInterest(
    val account: AccountWithCalculation,
    val dailyInterestMinor: Long,
)

fun List<AccountWithCalculation>.dailyInterestBreakdown(
    includeTypes: Set<InvestmentType>? = null,
): List<AccountDailyInterest> =
    filter { includeTypes == null || it.summary.type in includeTypes }
        .filter { it.summary.type.usesInterestAccrual() && it.calculation.status == CalculationStatus.OK }
        .map { AccountDailyInterest(it, it.calculation.estimatedDailyInterestMinor()) }
        .filter { it.dailyInterestMinor > 0L }
        .sortedByDescending { it.dailyInterestMinor }

fun List<AccountWithCalculation>.estimatedTotalFor(type: InvestmentType): Long =
    filter { it.summary.type == type && it.calculation.status == CalculationStatus.OK }
        .sumOf { it.calculation.estimatedTotalMinor }

fun List<AccountWithCalculation>.countFor(type: InvestmentType): Int =
    count { it.summary.type == type }
