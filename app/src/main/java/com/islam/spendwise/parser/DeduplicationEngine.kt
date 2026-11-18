package com.islam.spendwise.parser

import java.security.MessageDigest
import java.util.Locale

/** Deduplicates the same transaction received via SMS and notification channels. */
object DeduplicationEngine {

    private val nonAlnum = Regex("""[^\p{L}\p{N}]+""")

    /**
     * Canonical merchant key: case/punctuation-insensitive, keeping only the leading
     * significant token. "CARREFOUR HYPERMARKET" and "Carrefour" both key to "carrefour".
     */
    fun merchantKey(merchant: String?): String {
        val normalized = (merchant ?: "")
            .lowercase(Locale.ROOT)
            .replace(nonAlnum, " ")
            .trim()
        if (normalized.isBlank()) return "__unknown__"
        return normalized.split(' ').first()
    }

    /** True when both merchants share their leading significant token (channel phrasing variance). */
    fun merchantsMatch(a: String?, b: String?): Boolean = merchantKey(a) == merchantKey(b)

    private fun normalizeReference(reference: String?): String? =
        reference?.trim()
            ?.lowercase(Locale.ROOT)
            ?.filter { it.isLetterOrDigit() }
            ?.takeIf { it.isNotBlank() }

    /**
     * Deterministic when a transaction [reference] is present (SMS and notification of the same
     * transaction carry the same ref) — merchant/bucket are ignored. Otherwise falls back to
     * amount + canonical merchant token + 5-minute bucket.
     */
    fun hashOf(amountMinor: Long, merchant: String?, epochMs: Long, reference: String? = null): String {
        val ref = normalizeReference(reference)
        val raw = if (ref != null) {
            "ref|$amountMinor|$ref"
        } else {
            val bucket = epochMs / 300000L // 5-minute bucket
            "m|$amountMinor|${merchantKey(merchant)}|$bucket"
        }
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(raw.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { String.format(Locale.ROOT, "%02x", it) }
    }
}
