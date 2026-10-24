package com.personal.investmenttracker.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class MoneyTest {

    @Test
    fun zeroMoney_formatsAsZeroRupees() {
        assertEquals("₹0.00", formatInr(Money.ZERO))
    }

    @Test
    fun fromRupees_preservesPaise() {
        val money = Money.fromRupees(BigDecimal("1234567.89"))
        assertEquals(123456789L, money.amountMinor)
        assertEquals("₹12,34,567.89", formatInr(money))
    }

    @Test
    fun sumMoney_addsMinorUnits() {
        val total = listOf(Money(100L), Money(250L)).sumMoney()
        assertEquals(350L, total.amountMinor)
    }
}
