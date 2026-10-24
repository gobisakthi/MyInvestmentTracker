package com.personal.investmenttracker.ui.rates

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Month
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestRateHistoryScreen(
    viewModel: InterestRateViewModel,
    onNavigateBack: () -> Unit,
) {
    val rates by viewModel.rates.collectAsStateWithLifecycle()
    var year by remember { mutableStateOf("2026") }
    var month by remember { mutableStateOf("4") }
    var ratePercent by remember { mutableStateOf("7.1") }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interest rate history") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Annual rate effective from the selected month.", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(year, { year = it }, label = { Text("Effective year") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(month, { month = it }, label = { Text("Effective month (1–12)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(ratePercent, { ratePercent = it }, label = { Text("Annual rate (%)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(note, { note = it }, label = { Text("Note (optional)") }, modifier = Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val y = year.toIntOrNull()
                    val m = month.toIntOrNull()
                    val pct = ratePercent.toBigDecimalOrNull()
                    if (y == null || m == null || pct == null || pct < BigDecimal.ZERO) {
                        error = "Enter valid year, month, and rate."
                        return@Button
                    }
                    val bps = pct.multiply(BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).intValueExact()
                    viewModel.save(y, m, bps, note.ifBlank { null }, null) {
                        error = viewModel.lastError
                        if (viewModel.lastError == null) ratePercent = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save rate period") }
            Spacer(Modifier.height(16.dp))
            LazyColumn {
                items(rates, key = { it.id }) { r ->
                    val ym = YearMonth.from(r.effectiveFrom)
                    val pct = r.annualRateBasisPoints / 100.0
                    Text(
                        "${Month.of(ym.monthValue).name} ${ym.year}: $pct% p.a.",
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
        }
    }
}
