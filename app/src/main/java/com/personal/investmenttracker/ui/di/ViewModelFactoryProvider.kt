package com.personal.investmenttracker.ui.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.personal.investmenttracker.InvestmentTrackerApplication
import com.personal.investmenttracker.di.InvestmentViewModelFactory

@Composable
fun investmentViewModelFactory(): InvestmentViewModelFactory {
    val app = LocalContext.current.applicationContext as InvestmentTrackerApplication
    return app.viewModelFactory
}
