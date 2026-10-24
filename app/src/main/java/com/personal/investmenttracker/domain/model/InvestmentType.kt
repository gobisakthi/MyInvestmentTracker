package com.personal.investmenttracker.domain.model

enum class InvestmentType(val displayName: String) {
    PPF("Public Provident Fund (PPF)"),
    SSA("Sukanya Samriddhi Account (SSA)"),
    PF("Provident Fund (PF / EPF)"),
    FD("Fixed Deposit (FD)"),
    RD("Recurring Deposit (RD)"),
    STOCKS("Stocks"),
    METALS("Metals (gold / silver)"),
    REAL_ESTATE("Real estate"),
    NPS("NPS"),
    ;

    fun usesDepositsAndRateHistory(): Boolean = when (this) {
        PPF, SSA, PF -> true
        FD, RD, STOCKS, METALS, REAL_ESTATE, NPS -> false
    }

    fun usesInterestAccrual(): Boolean = when (this) {
        PPF, SSA, PF, FD, RD -> true
        STOCKS, METALS, REAL_ESTATE, NPS -> false
    }

    fun usesQuantityAndPrice(): Boolean = when (this) {
        STOCKS, METALS -> true
        else -> false
    }

    fun usesManualCurrentValue(): Boolean = when (this) {
        REAL_ESTATE, NPS -> true
        else -> false
    }
}
