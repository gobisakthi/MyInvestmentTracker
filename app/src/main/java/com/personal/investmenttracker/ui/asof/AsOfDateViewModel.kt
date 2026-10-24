package com.personal.investmenttracker.ui.asof

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AsOfDateViewModel : ViewModel() {
    var asOfDate by mutableStateOf(LocalDate.now())
        private set

    private val displayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

    fun refreshAsOfDate() {
        val today = LocalDate.now()
        if (asOfDate != today) {
            asOfDate = today
        }
    }

    fun formattedAsOfDate(): String = asOfDate.format(displayFormatter)
}
