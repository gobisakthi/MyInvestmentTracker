package com.personal.investmenttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.personal.investmenttracker.ui.navigation.InvestmentNavHost
import com.personal.investmenttracker.ui.theme.MyInvestmentTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyInvestmentTrackerTheme {
                InvestmentNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
