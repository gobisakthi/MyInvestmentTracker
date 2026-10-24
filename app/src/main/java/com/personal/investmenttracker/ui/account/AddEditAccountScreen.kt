package com.personal.investmenttracker.ui.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.investmenttracker.domain.model.InvestmentType
import com.personal.investmenttracker.ui.components.PremiumGlassCard
import com.personal.investmenttracker.ui.theme.TealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountScreen(
    accountId: Long?,
    viewModel: AddEditAccountViewModel,
    onNavigateBack: () -> Unit,
) {
    val isEdit = accountId != null
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val type = uiState.type

    LaunchedEffect(accountId) { viewModel.loadAccount(accountId) }

    LaunchedEffect(uiState.saveErrorMessage) {
        uiState.saveErrorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete account?") },
            text = { Text("This permanently removes the account and its data on this device.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteAccount(onNavigateBack)
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEdit) "Edit" else "New account",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            PremiumGlassCard {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = {
                        Text(
                            when (type) {
                                InvestmentType.STOCKS -> "Company name"
                                InvestmentType.REAL_ESTATE -> "Property name"
                                else -> "Name"
                            },
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                InvestmentTypeDropdown(selected = uiState.type, onSelected = viewModel::onTypeChange)

                when (type) {
                    InvestmentType.FD -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.openingBalance,
                            onValueChange = viewModel::onOpeningBalanceChange,
                            label = { Text("Principal (INR)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.balanceDate,
                            onValueChange = viewModel::onBalanceDateChange,
                            label = { Text("Start date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.termEndDate,
                            onValueChange = viewModel::onTermEndDateChange,
                            label = { Text("End date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                    InvestmentType.RD -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.rdMonthlyAmount,
                            onValueChange = viewModel::onRdMonthlyAmountChange,
                            label = { Text("Monthly investment (INR)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.rdStartYearMonth,
                            onValueChange = viewModel::onRdStartYearMonthChange,
                            label = { Text("Start month (YYYY-MM)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.rdEndYearMonth,
                            onValueChange = viewModel::onRdEndYearMonthChange,
                            label = { Text("End month (YYYY-MM)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                    InvestmentType.STOCKS -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.shareQuantity,
                            onValueChange = viewModel::onShareQuantityChange,
                            label = { Text("Number of shares") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.sharePrice,
                            onValueChange = viewModel::onSharePriceChange,
                            label = { Text("Current price per share (INR)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                    InvestmentType.METALS -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.shareQuantity,
                            onValueChange = viewModel::onShareQuantityChange,
                            label = { Text("Quantity") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.sharePrice,
                            onValueChange = viewModel::onSharePriceChange,
                            label = { Text("Price per unit (INR)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                    InvestmentType.REAL_ESTATE -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.currentValue,
                            onValueChange = viewModel::onCurrentValueChange,
                            label = { Text("Property value (INR)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                    InvestmentType.NPS -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.currentValue,
                            onValueChange = viewModel::onCurrentValueChange,
                            label = { Text("Current value (INR)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                    InvestmentType.PPF, InvestmentType.SSA, InvestmentType.PF -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.openingBalance,
                            onValueChange = viewModel::onOpeningBalanceChange,
                            label = { Text("Balance (INR)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.balanceDate,
                            onValueChange = viewModel::onBalanceDateChange,
                            label = { Text("As of date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                }

                val showRate = when (type) {
                    InvestmentType.FD, InvestmentType.RD -> true
                    InvestmentType.PPF, InvestmentType.SSA, InvestmentType.PF -> !isEdit
                    InvestmentType.STOCKS,
                    InvestmentType.METALS,
                    InvestmentType.REAL_ESTATE,
                    InvestmentType.NPS,
                    -> false
                }
                if (showRate) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = uiState.initialAnnualRatePercent,
                        onValueChange = viewModel::onInitialRateChange,
                        label = { Text("Annual rate (%)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChange,
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
            }

            if (uiState.validationErrors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                uiState.validationErrors.forEach { error ->
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    viewModel.save {
                        scope.launch { snackbarHostState.showSnackbar("Saved") }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            ) {
                Text(if (uiState.isSaving) "Saving…" else "Save")
            }

            if (isEdit) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving,
                ) {
                    Text("Delete account", color = Color(0xFFB91C1C))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvestmentTypeDropdown(
    selected: InvestmentType,
    onSelected: (InvestmentType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    androidx.compose.material3.ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Type") },
            trailingIcon = { androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        )
        androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            InvestmentType.entries.forEach { type ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(type.displayName) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    },
                )
            }
        }
    }
}
