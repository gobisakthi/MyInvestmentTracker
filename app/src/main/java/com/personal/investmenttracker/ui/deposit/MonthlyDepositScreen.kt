package com.personal.investmenttracker.ui.deposit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.investmenttracker.data.local.entity.MonthlyDepositEntity
import com.personal.investmenttracker.ui.components.PremiumGlassCard
import com.personal.investmenttracker.util.Money
import com.personal.investmenttracker.util.formatInrMinor
import java.time.Month

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyDepositScreen(
    viewModel: MonthlyDepositViewModel,
    onNavigateBack: () -> Unit,
) {
    val deposits by viewModel.deposits.collectAsStateWithLifecycle()
    var year by remember { mutableStateOf("2026") }
    var month by remember { mutableStateOf("5") }
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var editingId by remember { mutableLongStateOf(0L) }
    val isEditing = editingId != 0L
    var deleteTarget by remember { mutableStateOf<MonthlyDepositEntity?>(null) }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete deposit?") },
            text = {
                Text(
                    "${Month.of(target.month).name} ${target.year}: ${formatInrMinor(target.amountMinor)} " +
                        "will be removed. Interest estimates will update.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    deleteTarget = null
                    viewModel.delete(target.id) { err ->
                        error = err
                        if (err == null && editingId == target.id) {
                            editingId = 0L
                            amount = ""
                        }
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly deposits") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                "One deposit per month (PPF, SSA, PF). Tap a row to edit or delete.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            PremiumGlassCard {
                Text(
                    if (isEditing) "Edit deposit" else "Add deposit",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it; error = null },
                    label = { Text("Year") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it; error = null },
                    label = { Text("Month (1–12)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it; error = null },
                    label = { Text("Amount (INR)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        val y = year.toIntOrNull()
                        val m = month.toIntOrNull()
                        val money = Money.fromRupeesString(amount)
                        if (y == null || m == null || money == null) {
                            error = "Enter valid year, month, and amount."
                            return@Button
                        }
                        val existingId = editingId.takeIf { it != 0L }
                        viewModel.save(y, m, money.amountMinor, existingId) { err ->
                            error = err
                            if (err == null) {
                                editingId = 0L
                                amount = ""
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (isEditing) "Update deposit" else "Save deposit")
                }
                if (isEditing) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            editingId = 0L
                            amount = ""
                            error = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Cancel edit")
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Saved deposits", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (deposits.isEmpty()) {
                Text(
                    "No deposits yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(deposits, key = { it.id }) { d ->
                        DepositListRow(
                            deposit = d,
                            selected = d.id == editingId,
                            onEdit = {
                                editingId = d.id
                                year = d.year.toString()
                                month = d.month.toString()
                                amount = formatAmountInput(d.amountMinor)
                                error = null
                            },
                            onDelete = { deleteTarget = d },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DepositListRow(
    deposit: MonthlyDepositEntity,
    selected: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    PremiumGlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${Month.of(deposit.month).name} ${deposit.year}",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    formatInrMinor(deposit.amountMinor),
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (selected) {
                    Text(
                        "Editing…",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = "Edit deposit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete deposit")
            }
        }
    }
}

private fun formatAmountInput(amountMinor: Long): String {
    val rupees = amountMinor / 100
    val paise = amountMinor % 100
    return if (paise == 0L) rupees.toString() else "%d.%02d".format(rupees, paise)
}
