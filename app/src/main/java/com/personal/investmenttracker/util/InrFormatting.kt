package com.personal.investmenttracker.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Formats [money] as INR using Indian digit grouping, e.g. ₹12,34,567.89.
 */
fun formatInr(money: Money): String = formatInrMinorInternal(money.amountMinor)

fun formatInrMinor(amountMinor: Long): String = formatInrMinorInternal(amountMinor)

/**
 * Formats a rupee [BigDecimal] (allows negative amounts for future use).
 */
fun formatInrRupees(rupees: BigDecimal): String {
    val sign = if (rupees.signum() < 0) "-" else ""
    val absMinor = rupees.abs().setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact()
    return sign + formatInrMinorInternal(absMinor)
}

private fun formatInrMinorInternal(amountMinor: Long): String {
    val rupeesPart = amountMinor / 100
    val paisePart = (amountMinor % 100).toInt()
    val groupedRupees = formatIndianDigitGrouping(rupeesPart)
    return "₹$groupedRupees.${paisePart.toString().padStart(2, '0')}"
}

/**
 * Indian numbering: last three digits, then groups of two from the right.
 */
internal fun formatIndianDigitGrouping(rupeesWhole: Long): String {
    val digits = rupeesWhole.toString()
    if (digits.length <= 3) return digits
    val lastThree = digits.takeLast(3)
    var rest = digits.dropLast(3)
    val groups = mutableListOf<String>()
    while (rest.isNotEmpty()) {
        val take = minOf(2, rest.length)
        groups.add(0, rest.takeLast(take))
        rest = rest.dropLast(take)
    }
    return groups.joinToString(",") + "," + lastThree
}
