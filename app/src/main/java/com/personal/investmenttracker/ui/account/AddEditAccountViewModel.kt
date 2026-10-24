package com.personal.investmenttracker.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.investmenttracker.data.repository.AccountRepository
import com.personal.investmenttracker.domain.model.InvestmentType
import com.personal.investmenttracker.domain.validation.AccountFormValidationResult
import com.personal.investmenttracker.domain.validation.AccountFormValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

data class AddEditAccountUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val type: InvestmentType = InvestmentType.PPF,
    val openingBalance: String = "",
    val balanceDate: String = "",
    val openingBalanceIncludesCreditedInterest: Boolean = false,
    val notes: String = "",
    val validationErrors: List<String> = emptyList(),
    val saveSucceeded: Boolean = false,
    val saveErrorMessage: String? = null,
)

class AddEditAccountViewModel(
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditAccountUiState())
    val uiState: StateFlow<AddEditAccountUiState> = _uiState.asStateFlow()

    private var loadedAccountId: Long? = null
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

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
            _uiState.update {
                it.copy(
                    isLoading = false,
                    name = entity.name,
                    type = entity.type,
                    openingBalance = formatRupeesInput(entity.openingBalanceMinor),
                    balanceDate = entity.openingBalanceDate.format(dateFormatter),
                    openingBalanceIncludesCreditedInterest = entity.openingBalanceIncludesCreditedInterest,
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

    fun onIncludesCreditedInterestChange(value: Boolean) =
        updateForm { copy(openingBalanceIncludesCreditedInterest = value) }

    fun onNotesChange(value: String) = updateForm { copy(notes = value) }

    fun consumeSaveSuccess() = updateForm { copy(saveSucceeded = false) }

    fun save(onSuccess: () -> Unit) {
        val current = _uiState.value
        when (
            val result = AccountFormValidator.validate(
                name = current.name,
                type = current.type,
                openingBalanceInput = current.openingBalance,
                balanceDateInput = current.balanceDate,
                openingBalanceIncludesCreditedInterest = current.openingBalanceIncludesCreditedInterest,
                notes = current.notes,
            )
        ) {
            is AccountFormValidationResult.Invalid -> {
                _uiState.update { it.copy(validationErrors = result.errors) }
            }
            is AccountFormValidationResult.Valid -> {
                viewModelScope.launch {
                    _uiState.update {
                        it.copy(isSaving = true, validationErrors = emptyList(), saveErrorMessage = null)
                    }
                    try {
                        val accountId = loadedAccountId
                        if (accountId == null) {
                            accountRepository.insertAccount(result.form)
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

    private fun formatRupeesInput(amountMinor: Long): String {
        val rupees = amountMinor / 100
        val paise = amountMinor % 100
        return if (paise == 0L) rupees.toString() else "%d.%02d".format(rupees, paise)
    }
}
