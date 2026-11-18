package com.islam.spendwise.parser

/** Result of attempting to parse a raw transaction message. */
sealed interface ParserResult {
    data class Success(val transaction: ParsedTransaction, val ruleId: String) : ParserResult
    data class NotATransaction(val reason: String) : ParserResult
    data class Unparsed(val candidateAmount: Long?, val reason: String) : ParserResult
}

data class ParsedTransaction(
    val amountMinor: Long,
    val type: TransactionType,
    val merchant: String?,
    val occurredAt: Long,
    val reference: String?
)