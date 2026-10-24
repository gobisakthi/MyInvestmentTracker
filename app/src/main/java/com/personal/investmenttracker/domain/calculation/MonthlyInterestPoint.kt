package com.personal.investmenttracker.domain.calculation

import java.time.YearMonth

data class MonthlyInterestPoint(
    val yearMonth: YearMonth,
    val interestMinor: Long,
    val isProvisional: Boolean,
)
