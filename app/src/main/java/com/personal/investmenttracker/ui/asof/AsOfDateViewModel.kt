package com.personal.investmenttracker.ui.asof

import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AsOfDateViewModel : ViewModel() {
    private val displayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    private var followToday = true

    private val _asOfDate = MutableStateFlow(LocalDate.now())
    val asOfDate: StateFlow<LocalDate> = _asOfDate.asStateFlow()

    fun refreshAsOfDate() {
        if (followToday) {
            _asOfDate.value = LocalDate.now()
        }
    }

    fun setAsOfDate(date: LocalDate) {
        followToday = false
        _asOfDate.value = date
    }

    fun resetToToday() {
        followToday = true
        _asOfDate.value = LocalDate.now()
    }

    fun formattedAsOfDate(): String = _asOfDate.value.format(displayFormatter)
}
