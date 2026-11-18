package com.islam.spendwise.parser

import com.islam.spendwise.core.CurrencyUtils
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Extracts structured transaction data from raw bank SMS / notification text.
 * Works across Egyptian banks (CIB, NBE, QNB, Banque Misr, InstaPay, etc.).
 */
class RegexTransactionParser {

    private val debitHints = listOf("purchase", "debit", "withdraw", "payment", "paid", "خصم", "دفع", "سحب", "مشتريات", "سداد", "شراء")
    private val creditHints = listOf("credit", "refund", "deposit", "received", "إيداع", "مرتجع", "دائن", "استلام")

    fun parse(input: String): ParserResult {
        val normalized = CurrencyUtils.normalizeDigits(input)
        val lower = normalized.lowercase(Locale.ROOT)

        val hasCurrency = listOf("egp", "e£", "ج.م", "جنيه").any { lower.contains(it) } ||
            Regex("""ج\.?\s?م""").containsMatchIn(normalized)
        if (!hasCurrency) {
            return ParserResult.NotATransaction("No EGP currency marker")
        }

        val hasTxnWord = debitHints.any { lower.contains(it) } || creditHints.any { lower.contains(it) }
        if (!hasTxnWord) {
            return ParserResult.NotATransaction("No transaction keyword")
        }

        val type = when {
            debitHints.any { lower.contains(it) } -> TransactionType.DEBIT
            else -> TransactionType.CREDIT
        }

        val amountMinor = extractAmount(normalized) ?: return ParserResult.Unparsed(null, "No amount matched")
        if (amountMinor <= 0) return ParserResult.Unparsed(amountMinor, "Amount must be positive")

        val occurredAt = extractTimestamp(normalized)
        val merchant = extractMerchant(normalized)
        val reference = extractReference(normalized)

        return ParserResult.Success(
            ParsedTransaction(
                amountMinor = amountMinor,
                type = type,
                merchant = merchant,
                occurredAt = occurredAt,
                reference = reference
            ),
            ruleId = "generic-en-eg"
        )
    }

    private fun extractAmount(normalized: String): Long? {
        val regex = Regex("""(?:EGP|E£|ج\.?\s?م|جنيه)\s*([0-9][0-9,\.٫]*)""", RegexOption.IGNORE_CASE)
        val match = regex.find(normalized)
            ?: Regex("""([0-9][0-9,\.]*)\s*(?:EGP|E£|ج\.?\s?م|جنيه)""", RegexOption.IGNORE_CASE).find(normalized)
            ?: return null
        return CurrencyUtils.parseAmountToMinor(match.groupValues[1])
    }

    private fun extractTimestamp(normalized: String): Long {
        val regex = Regex("""(\d{1,2})[/-](\d{1,2})[/-](\d{4})(?:\s+(\d{1,2}):(\d{2}))?""")
        val match = regex.find(normalized) ?: Regex("""(\d{4})[/-](\d{1,2})[/-](\d{1,2})""").find(normalized) ?: return System.currentTimeMillis()

        return try {
            val isYearFirst = match.value.matches(Regex("""\d{4}[/-].*"""))
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Africa/Cairo")).apply {
                clear()
                if (isYearFirst) {
                    set(Calendar.YEAR, match.groupValues[1].toInt())
                    set(Calendar.MONTH, match.groupValues[2].toInt() - 1)
                    set(Calendar.DAY_OF_MONTH, match.groupValues[3].toInt())
                } else {
                    set(Calendar.DAY_OF_MONTH, match.groupValues[1].toInt())
                    set(Calendar.MONTH, match.groupValues[2].toInt() - 1)
                    set(Calendar.YEAR, match.groupValues[3].toInt())
                    if (match.groupValues.size > 4 && match.groupValues[4].isNotEmpty()) {
                        set(Calendar.HOUR_OF_DAY, match.groupValues[4].toInt())
                        set(Calendar.MINUTE, match.groupValues[5].toInt())
                    }
                }
            }
            calendar.timeInMillis
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun extractReference(normalized: String): String? {
        val regex = Regex("""(?:ref(?:erence)?|رقم\s*العملية)\s*[:#.\-]?\s*([A-Za-z0-9\-/]{4,})""", RegexOption.IGNORE_CASE)
        return regex.find(normalized)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun extractMerchant(normalized: String): String? {
        val patterns = listOf(
            Regex("""(?:at|from|to|لدى|في|من)\s+([^\n\r]{2,40}?)(?=\s+(?:on|at|\d)|\.|$)""", RegexOption.IGNORE_CASE),
            Regex("""\bat\s+([A-Za-z0-9&\-\s]{2,30})""", RegexOption.IGNORE_CASE)
        )
        patterns.forEach { regex ->
            val match = regex.find(normalized) ?: return@forEach
            val candidate = match.groupValues[1].trim().trimEnd('.', ',')
            if (candidate.length >= 2) return candidate
        }
        return null
    }
}