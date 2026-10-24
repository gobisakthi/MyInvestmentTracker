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
import com.personal.investmenttracker.ui.asof.AsOfDateViewModel
import com.personal.investmenttracker.ui.components.MonthlyInterestBarChart
import com.personal.investmenttracker.ui.components.PremiumGlassCard
import com.personal.investmenttracker.ui.components.PremiumHeroCard
import com.personal.investmenttracker.ui.components.colorForInvestmentType
import com.personal.investmenttracker.util.formatInrMinor

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
                        Text(
                            asOfDateViewModel.formattedAsOfDate(),
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    PremiumGlassCard {
                        Text("Monthly interest", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        MonthlyInterestBarChart(points = row.monthlyBreakdown)
                    }
                    Spacer(Modifier.height(16.dp))
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
                    OutlinedButton(onClick = onEditAccount, modifier = Modifier.fillMaxWidth()) {
                        Text("Edit account")
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
