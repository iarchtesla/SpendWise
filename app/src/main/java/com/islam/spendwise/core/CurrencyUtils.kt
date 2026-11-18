package com.islam.spendwise.core

import java.util.Locale

/**
 * Currency and text helpers for the parser.
 */
object CurrencyUtils {

    fun normalizeDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            when {
                ch in '٠'..'٩' -> sb.append('0' + (ch - '٠'))   // Arabic-Indic
                ch in '۰'..'۹' -> sb.append('0' + (ch - '۰'))   // Extended (Persian)
                ch == '٫' -> sb.append('.')                      // Arabic decimal separator
                ch == '٬' -> sb.append(',')                      // Arabic thousands separator
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun parseAmountToMinor(input: String?): Long? {
        if (input.isNullOrBlank()) return null
        val normalized = normalizeDigits(input)
        val cleaned = normalized.filter { it.isDigit() || it == '.' || it == ',' }
        if (cleaned.isBlank()) return null

        // Grouping validation: separators followed by exactly 3 digits (e.g. "1,250", "١٢٬٥٠٠")
        // are thousands separators, not decimals. EGP has no 3-digit fraction (piastre = 1/100).
        val groups = cleaned.split('.', ',')
        val isThousandsGrouped = groups.size > 1 &&
            groups[0].length in 1..3 &&
            groups.drop(1).all { it.length == 3 }
        if (isThousandsGrouped) {
            val whole = groups.joinToString("").toLongOrNull() ?: return null
            return if (whole > Long.MAX_VALUE / 100) null else whole * 100
        }

        val lastDot = cleaned.lastIndexOf('.')
        val lastComma = cleaned.lastIndexOf(',')
        val sepIndex = maxOf(lastDot, lastComma)
        val intPart = if (sepIndex >= 0) {
            cleaned.substring(0, sepIndex).filter { it.isDigit() }
        } else {
            cleaned.filter { it.isDigit() }
        }
        if (intPart.isBlank()) return null
        val fracPart = if (sepIndex >= 0) {
            cleaned.substring(sepIndex + 1)
                .filter { it.isDigit() }
                .padEnd(2, '0')
                .take(2)
        } else "00"
        val whole = intPart.toLongOrNull() ?: return null
        val fraction = fracPart.toLongOrNull() ?: return null
        return whole * 100 + fraction
    }

    fun minorToDouble(minor: Long) = minor / 100.0

    fun formatEgp(minor: Long) = String.format(Locale.ROOT, "EGP %.2f", minorToDouble(minor))
}