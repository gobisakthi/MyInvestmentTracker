package com.personal.investmenttracker.ui.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.investmenttracker.domain.calculation.CalculationStatus
import com.personal.investmenttracker.domain.model.InvestmentType
import com.personal.investmenttracker.ui.asof.AsOfDateViewModel
import com.personal.investmenttracker.ui.components.MonthlyInterestBarChart
import com.personal.investmenttracker.ui.components.PremiumGlassCard
import com.personal.investmenttracker.ui.components.PremiumHeroCard
import com.personal.investmenttracker.ui.components.colorForInvestmentType
import com.personal.investmenttracker.util.formatInrMinor
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    viewModel: AccountDetailViewModel,
    asOfDateViewModel: AsOfDateViewModel,
    onNavigateBack: () -> Unit,
    onEditAccount: () -> Unit,
    onMonthlyDeposits: () -> Unit,
    onInterestRateHistory: () -> Unit,
) {
    val evaluation by viewModel.evaluation.collectAsStateWithLifecycle()
    val asOf by asOfDateViewModel.asOfDate.collectAsStateWithLifecycle()
    LaunchedEffect(asOf) { viewModel.updateAsOfDate(asOf) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(evaluation?.summary?.name ?: "") },
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
    ) { innerPadding ->
        when (val row = evaluation) {
            null -> {
                Column(
                    Modifier.fillMaxSize().padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(Modifier.padding(48.dp))
                }
            }
            else -> {
                val calc = row.calculation
                val accent = colorForInvestmentType(row.summary.type)
                val type = row.summary.type
                val isQuantityPrice = type.usesQuantityAndPrice()
                val isManualValue = type.usesManualCurrentValue()
                val showDepositsAndRates = type.usesDepositsAndRateHistory()
                val showInterestChart = type.usesInterestAccrual()
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    PremiumHeroCard {
                        Text(
                            row.summary.type.name,
                            color = accent.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(8.dp))
                        if (calc.status == CalculationStatus.OK) {
                            Text(
                                formatInrMinor(calc.estimatedTotalMinor),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium,
                            )
                        } else {
                            Text(
                                calc.messages.firstOrNull() ?: "Unavailable",
                                color = Color.White.copy(alpha = 0.9f),
                            )
                        }
                        if (!isQuantityPrice && !isManualValue) {
                            Text(
                                asOfDateViewModel.formattedAsOfDate(),
                                color = Color.White.copy(alpha = 0.75f),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    if (isQuantityPrice && row.shareQuantity != null && row.currentSharePriceMinor != null) {
                        PremiumGlassCard {
                            Text("Holdings", style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(8.dp))
                            val qtyText = BigDecimal.valueOf(row.shareQuantity)
                                .stripTrailingZeros()
                                .toPlainString()
                            val unitLabel = if (type == InvestmentType.STOCKS) "shares" else "units"
                            Text(
                                "$qtyText $unitLabel × ${formatInrMinor(row.currentSharePriceMinor)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                if (type == InvestmentType.STOCKS) {
                                    "Market value = shares × latest price you entered"
                                } else {
                                    "Value = quantity × price per unit you entered"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                    if (isManualValue && row.currentValueMinor != null) {
                        PremiumGlassCard {
                            Text(
                                if (type == InvestmentType.REAL_ESTATE) "Property value" else "Current value",
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                formatInrMinor(row.currentValueMinor),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                "Update via Edit when your estimate changes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                    if (showInterestChart) {
                        PremiumGlassCard {
                            Text("Monthly interest", style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(8.dp))
                            MonthlyInterestBarChart(points = row.monthlyBreakdown)
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                    if (showDepositsAndRates) {
                        Button(
                            onClick = onMonthlyDeposits,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = accent),
                        ) {
                            Text("Deposits")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = onInterestRateHistory, modifier = Modifier.fillMaxWidth()) {
                            Text("Rates")
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    OutlinedButton(onClick = onEditAccount, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            when {
                                type == InvestmentType.STOCKS -> "Update shares & price"
                                type == InvestmentType.METALS -> "Update quantity & price"
                                isManualValue -> "Update value"
                                else -> "Edit account"
                            },
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
