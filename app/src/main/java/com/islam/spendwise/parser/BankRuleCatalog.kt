package com.islam.spendwise.parser

import com.islam.spendwise.core.CurrencyUtils
import java.util.Locale

/** Known Egyptian bank sender IDs used as a fast pre-filter. */
object BankRuleCatalog {
    private val knownSenders = listOf(
        "cib", "nbe", "qnb", "banque", "aaib", "msg", "emoney",
        "instapay", "fawry", "valu", "meeza", "aman"
    )

    // \d is ASCII-only in Java/Kotlin regex, so normalize Arabic-Indic digits to ASCII first,
    // then require at least two digits (a lone "3" in "3 branches" is not an amount).
    private val amountHint = Regex("""[0-9][0-9,\.\s٬٫]{1,}""")

    fun isLikelyBankSender(sender: String?): Boolean {
        if (sender.isNullOrBlank()) return false
        val lower = sender.lowercase(Locale.ROOT)
        return knownSenders.any { lower.contains(it) } || lower.contains("bank") || lower.contains("instapay")
    }

    fun looksTransactional(body: String): Boolean {
        val normalized = CurrencyUtils.normalizeDigits(body)
        val lower = normalized.lowercase(Locale.ROOT)
        val hasAmount = amountHint.containsMatchIn(normalized)
        val actionHint = listOf(
            "debit", "credit", "purchase", "transfer", "withdraw", "deposit", "paid", "sent", "received",
            "خصم", "دائن", "تحويل", "دفع", "مشتريات", "سحب", "إيداع", "استلام", "تم ارسال"
        ).any { lower.contains(it) }
        return hasAmount && actionHint
    }
}
