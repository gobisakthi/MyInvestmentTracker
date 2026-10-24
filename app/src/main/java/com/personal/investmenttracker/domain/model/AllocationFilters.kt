package com.personal.investmenttracker.domain.model

val traditionalAllocationTypes: Set<InvestmentType> = setOf(
    InvestmentType.PPF,
    InvestmentType.SSA,
    InvestmentType.PF,
    InvestmentType.FD,
    InvestmentType.RD,
)

val providentPlusStocksAllocationTypes: Set<InvestmentType> =
    traditionalAllocationTypes + InvestmentType.STOCKS

fun allocationTypesForDashboardPage(page: Int): Set<InvestmentType>? = when (page) {
    0 -> traditionalAllocationTypes
    1 -> providentPlusStocksAllocationTypes
    else -> null
}

fun dashboardAllocationSubtitle(page: Int): String = when (page) {
    0 -> "PPF · SSA · PF · FD · RD"
    1 -> "PPF · SSA · PF · FD · RD + stocks"
    else -> "All holdings"
}

const val DASHBOARD_ALLOCATION_PAGE_COUNT = 3
