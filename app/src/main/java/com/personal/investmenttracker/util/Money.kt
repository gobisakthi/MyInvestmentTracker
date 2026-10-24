package com.personal.investmenttracker.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Monetary amount stored as integer paise (1 rupee = 100 paise).
 */
@JvmInline
value class Money(val amountMinor: Long) {
    init {
        require(amountMinor >= 0) { "Money amount must be non-negative" }
    }

    operator fun plus(other: Money): Money = Money(amountMinor + other.amountMinor)

    fun toBigDecimalRupees(): BigDecimal =
        BigDecimal.valueOf(amountMinor, 2)

    companion object {
        val ZERO = Money(0L)

        fun fromRupees(rupees: BigDecimal): Money {
            val scaled = rupees.setScale(2, RoundingMode.HALF_UP)
            val minor = scaled.movePointRight(2).longValueExact()
            return Money(minor)
        }

        fun fromRupeesString(rupees: String): Money? {
            val trimmed = rupees.trim()
            if (trimmed.isEmpty()) return null
            return try {
                fromRupees(BigDecimal(trimmed))
            } catch (_: Exception) {
                null
            }
        }
    }
}

fun Iterable<Money>.sumMoney(): Money = fold(Money.ZERO) { acc, money -> acc + money }
