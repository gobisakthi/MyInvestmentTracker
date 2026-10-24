package com.personal.investmenttracker.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.investmenttracker.data.repository.AccountRepository
import com.personal.investmenttracker.data.repository.InterestRateRepository
import com.personal.investmenttracker.domain.calculation.firstAccrualYearMonth
import com.personal.investmenttracker.domain.model.InvestmentType
import com.personal.investmenttracker.domain.validation.AccountFormValidationResult
import com.personal.investmenttracker.domain.validation.AccountFormValidator
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddEditAccountUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val type: InvestmentType = InvestmentType.PPF,
    val openingBalance: String = "",
    val balanceDate: String = "",
    val termEndDate: String = "",
    val rdMonthlyAmount: String = "",
    val rdStartYearMonth: String = "",
    val rdEndYearMonth: String = "",
    val initialAnnualRatePercent: String = "7.1",
    val shareQuantity: String = "",
    val sharePrice: String = "",
    val currentValue: String = "",
    val openingBalanceIncludesCreditedInterest: Boolean = true,
    val notes: String = "",
    val validationErrors: List<String> = emptyList(),
    val saveSucceeded: Boolean = false,
    val saveErrorMessage: String? = null,
)

class AddEditAccountViewModel(
    private val accountRepository: AccountRepository,
    private val interestRateRepository: InterestRateRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditAccountUiState())
    val uiState: StateFlow<AddEditAccountUiState> = _uiState.asStateFlow()

    private var loadedAccountId: Long? = null
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    fun loadAccount(accountId: Long?) {
        if (accountId == null) {
            loadedAccountId = null
            _uiState.value = AddEditAccountUiState()
            return
        }
        if (loadedAccountId == accountId && _uiState.value.name.isNotEmpty()) return
        loadedAccountId = accountId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, validationErrors = emptyList()) }
            val entity = accountRepository.getAccount(accountId)
            if (entity == null) {
                _uiState.update {
                    it.copy(isLoading = false, saveErrorMessage = "Account not found.")
                }
                return@launch
            }
            val ratePercent = entity.fixedAnnualRateBasisPoints?.let { bps ->
                BigDecimal(bps).divide(BigDecimal(100), 2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
            }.orEmpty()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    name = entity.name,
                    type = entity.type,
                    openingBalance = formatRupeesInput(entity.openingBalanceMinor),
                    balanceDate = entity.openingBalanceDate.format(dateFormatter),
                    termEndDate = entity.termEndDate?.format(dateFormatter).orEmpty(),
                    rdMonthlyAmount = entity.rdMonthlyAmountMinor?.let { m -> formatRupeesInput(m) }.orEmpty(),
                    rdStartYearMonth = entity.termStartYear?.let { y ->
                        entity.termStartMonth?.let { m -> YearMonth.of(y, m).format(yearMonthFormatter) }
                    }.orEmpty(),
                    rdEndYearMonth = entity.termEndYear?.let { y ->
                        entity.termEndMonth?.let { m -> YearMonth.of(y, m).format(yearMonthFormatter) }
                    }.orEmpty(),
                    initialAnnualRatePercent = ratePercent.ifEmpty { it.initialAnnualRatePercent },
                    shareQuantity = entity.shareQuantity?.let { q -> formatShareQuantity(q) }.orEmpty(),
                    sharePrice = entity.currentSharePriceMinor?.let { m -> formatRupeesInput(m) }.orEmpty(),
                    currentValue = entity.currentValueMinor?.let { m -> formatRupeesInput(m) }.orEmpty(),
                    openingBalanceIncludesCreditedInterest = true,
                    notes = entity.notes.orEmpty(),
                    validationErrors = emptyList(),
                    saveErrorMessage = null,
                )
            }
        }
    }

    fun onNameChange(value: String) = updateForm { copy(name = value, validationErrors = emptyList()) }

    fun onTypeChange(value: InvestmentType) = updateForm { copy(type = value, validationErrors = emptyList()) }

    fun onOpeningBalanceChange(value: String) =
        updateForm { copy(openingBalance = value, validationErrors = emptyList()) }

    fun onBalanceDateChange(value: String) =
        updateForm { copy(balanceDate = value, validationErrors = emptyList()) }

    fun onTermEndDateChange(value: String) =
        updateForm { copy(termEndDate = value, validationErrors = emptyList()) }

    fun onRdMonthlyAmountChange(value: String) =
        updateForm { copy(rdMonthlyAmount = value, validationErrors = emptyList()) }

    fun onRdStartYearMonthChange(value: String) =
        updateForm { copy(rdStartYearMonth = value, validationErrors = emptyList()) }

    fun onRdEndYearMonthChange(value: String) =
        updateForm { copy(rdEndYearMonth = value, validationErrors = emptyList()) }

    fun onInitialRateChange(value: String) =
        updateForm { copy(initialAnnualRatePercent = value, validationErrors = emptyList()) }

    fun onShareQuantityChange(value: String) =
        updateForm { copy(shareQuantity = value, validationErrors = emptyList()) }

    fun onSharePriceChange(value: String) =
        updateForm { copy(sharePrice = value, validationErrors = emptyList()) }

    fun onCurrentValueChange(value: String) =
        updateForm { copy(currentValue = value, validationErrors = emptyList()) }

    fun onNotesChange(value: String) = updateForm { copy(notes = value) }

    fun save(onSuccess: () -> Unit) {
        val current = _uiState.value
        when (
            val result = AccountFormValidator.validate(
                name = current.name,
                type = current.type,
                openingBalanceInput = current.openingBalance,
                balanceDateInput = current.balanceDate,
                openingBalanceIncludesCreditedInterest = true,
                notes = current.notes,
                annualRatePercentInput = current.initialAnnualRatePercent,
                termEndDateInput = current.termEndDate,
                rdMonthlyAmountInput = current.rdMonthlyAmount,
                rdStartYearMonthInput = current.rdStartYearMonth,
                rdEndYearMonthInput = current.rdEndYearMonth,
                shareQuantityInput = current.shareQuantity,
                sharePriceInput = current.sharePrice,
                currentValueInput = current.currentValue,
            )
        ) {
            is AccountFormValidationResult.Invalid -> {
                _uiState.update { it.copy(validationErrors = result.errors) }
            }
            is AccountFormValidationResult.Valid -> {
                val isNew = loadedAccountId == null
                val needsInitialRateRow = current.type.usesDepositsAndRateHistory()
                if (isNew && needsInitialRateRow && rateBpsFromPercent(current.initialAnnualRatePercent) == null) {
                    _uiState.update {
                        it.copy(validationErrors = listOf("Enter a valid initial annual interest rate (%)."))
                    }
                    return
                }
                viewModelScope.launch {
                    _uiState.update {
                        it.copy(isSaving = true, validationErrors = emptyList(), saveErrorMessage = null)
                    }
                    try {
                        val accountId = loadedAccountId
                        if (accountId == null) {
                            val newId = accountRepository.insertAccount(result.form)
                            seedRateHistoryIfNeeded(newId, result.form, current.initialAnnualRatePercent)
                        } else {
                            accountRepository.updateAccount(accountId, result.form)
                        }
                        _uiState.update { it.copy(isSaving = false, saveSucceeded = true) }
                        onSuccess()
                    } catch (e: Exception) {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                saveErrorMessage = e.message ?: "Could not save account.",
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun seedRateHistoryIfNeeded(
        accountId: Long,
        form: com.personal.investmenttracker.domain.validation.ValidatedAccountForm,
        ratePercentInput: String,
    ) {
        if (!form.type.usesDepositsAndRateHistory()) return
        val bps = rateBpsFromPercent(ratePercentInput) ?: return
        val effective = firstAccrualYearMonth(form.openingBalanceDate)
        interestRateRepository.upsert(
            accountId = accountId,
            effectiveYear = effective.year,
            effectiveMonth = effective.monthValue,
            annualRateBasisPoints = bps,
            sourceNote = "Initial rate at account setup",
            existingId = null,
        ).getOrThrow()
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        val accountId = loadedAccountId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveErrorMessage = null) }
            try {
                accountRepository.deleteAccount(accountId)
                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveErrorMessage = e.message ?: "Could not delete account.",
                    )
                }
            }
        }
    }

    private fun updateForm(block: AddEditAccountUiState.() -> AddEditAccountUiState) {
        _uiState.update(block)
    }

    private fun rateBpsFromPercent(input: String): Int? {
        val percent = input.trim().toBigDecimalOrNull() ?: return null
        if (percent < BigDecimal.ZERO) return null
        return percent.multiply(BigDecimal(100))
            .setScale(0, RoundingMode.HALF_UP)
            .intValueExact()
    }

    private fun formatShareQuantity(quantity: Double): String =
        BigDecimal.valueOf(quantity).stripTrailingZeros().toPlainString()

    private fun formatRupeesInput(amountMinor: Long): String {
        val rupees = amountMinor / 100
        val paise = amountMinor % 100
        return if (paise == 0L) rupees.toString() else "%d.%02d".format(rupees, paise)
    }
}
