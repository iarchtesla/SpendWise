package com.islam.spendwise.service

import com.islam.spendwise.data.local.SpendWiseDatabaseProvider
import com.islam.spendwise.data.local.entity.TransactionEntity
import com.islam.spendwise.parser.DeduplicationEngine
import com.islam.spendwise.parser.ParsedTransaction
import com.islam.spendwise.parser.ParserResult
import com.islam.spendwise.parser.RegexTransactionParser
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Parses raw SMS/notification text and persists a [TransactionEntity] when it looks
 * like a bank transaction. Designed to be called from background receivers.
 */
object TransactionIngestor {

    private val parser = RegexTransactionParser()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun ingest(
        body: String,
        sender: String?,
        source: String,
        isInstapay: Boolean = false,
        onComplete: (() -> Unit)? = null
    ) {
        val result = try {
            parser.parse(body, sender)
        } catch (_: Exception) {
            onComplete?.invoke()
            return
        }
        when (result) {
            is ParserResult.Success -> persist(result.transaction, body, sender, source, isInstapay, onComplete)
            is ParserResult.Unparsed -> onComplete?.invoke()
            is ParserResult.NotATransaction -> onComplete?.invoke()
        }
    }

    private fun persist(
        parsed: ParsedTransaction,
        raw: String,
        sender: String?,
        source: String,
        isInstapay: Boolean,
        onComplete: (() -> Unit)?
    ) {
        val dedupeHash = DeduplicationEngine.hashOf(parsed.amountMinor, parsed.merchant, parsed.occurredAt, parsed.reference)
        scope.launch {
            try {
                val categoryId = autoCategorize(raw)
                val entity = TransactionEntity(
                    amountMinor = parsed.amountMinor,
                    currency = "EGP",
                    categoryId = categoryId,
                    merchant = parsed.merchant,
                    source = if (isInstapay) "INSTAPAY" else source,
                    type = parsed.type.name,
                    occurredAt = parsed.occurredAt,
                    rawMessage = raw,
                    sender = sender,
                    isReviewed = false,
                    isUserCorrected = false,
                    dedupeHash = dedupeHash
                )
                SpendWiseDatabaseProvider.database.transactionDao().insert(entity)
            } catch (_: Exception) {
                // Best effort; duplicates or DB failures are ignored in the background path.
            } finally {
                onComplete?.invoke()
            }
        }
    }

    /** Finds the first matching enabled rule (lowest priority wins) against the raw body text. */
    private suspend fun autoCategorize(body: String): Long? {
        return try {
            val lower = body.lowercase(Locale.ROOT)
            SpendWiseDatabaseProvider.database.categoryRuleDao().getEnabledOrdered()
                .firstOrNull { rule ->
                    if (rule.isRegex) {
                        runCatching { Regex(rule.keyword, RegexOption.IGNORE_CASE).containsMatchIn(body) }.getOrDefault(false)
                    } else {
                        lower.contains(rule.keyword.lowercase(Locale.ROOT))
                    }
                }
                ?.categoryId
        } catch (_: Exception) {
            null
        }
    }
}
