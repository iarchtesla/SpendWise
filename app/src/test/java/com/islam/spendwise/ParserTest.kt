package com.islam.spendwise

import com.islam.spendwise.core.CurrencyUtils
import com.islam.spendwise.parser.BankRuleCatalog
import com.islam.spendwise.parser.DeduplicationEngine
import com.islam.spendwise.parser.ParserResult
import com.islam.spendwise.parser.RegexTransactionParser
import com.islam.spendwise.parser.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ParserTest {

    private val parser = RegexTransactionParser()

    @Test
    fun parsesCibStyleDebitSms() {
        val body = "CIB: Purchase of EGP 1,250.00 at CARREFOUR on 15/11/2026 14:30 was approved. Ref 123456"
        val result = parser.parse(body)
        assertTrue(result is ParserResult.Success)
        val tx = (result as ParserResult.Success).transaction
        assertEquals(125000L, tx.amountMinor)
        assertEquals(TransactionType.DEBIT, tx.type)
    }

    @Test
    fun parsesArabicIndicDigitsAndArabicCurrency() {
        val body = "خصم ١٢٥٠٫٠٠ ج.م من بطاقتك لدى كارفور"
        val result = parser.parse(body)
        assertTrue(result is ParserResult.Success)
        val tx = (result as ParserResult.Success).transaction
        assertEquals(125000L, tx.amountMinor)
        assertEquals(TransactionType.DEBIT, tx.type)
    }

    @Test
    fun parsesArabicDateFormat() {
        val body = "Vodafone: Your bill EGP 350.00 was paid successfully on 03/11/2026 09:15"
        val result = parser.parse(body)
        assertTrue(result is ParserResult.Success)
        val tx = (result as ParserResult.Success).transaction
        assertEquals(35000L, tx.amountMinor)
    }

    @Test
    fun rejectsNonTransactionMessage() {
        val result = parser.parse("Hello! Enjoy your free weekend offer at our new branch.")
        assertTrue(result is ParserResult.NotATransaction)
    }

    @Test
    fun creditBecomesCreditType() {
        val body = "InstaPay: You received EGP 500.00 from Ahmed Ali on 10/11/2026 18:00"
        val result = parser.parse(body)
        assertTrue(result is ParserResult.Success)
        assertEquals(TransactionType.CREDIT, (result as ParserResult.Success).transaction.type)
    }

    @Test
    fun dedupeHashIsStableWithinFiveMinuteBucket() {
        val t = 1_700_000_000_000L
        val h1 = DeduplicationEngine.hashOf(125000L, "Carrefour", t)
        val h2 = DeduplicationEngine.hashOf(125000L, "carrefour", t + 60_000L)
        val h3 = DeduplicationEngine.hashOf(125000L, "Carrefour", t + 10 * 60_000L)
        assertEquals(h1, h2)
        assertTrue(h1 != h3)
    }

    @Test
    fun currencyUtilsRoundTrip() {
        assertEquals(125000L, CurrencyUtils.parseAmountToMinor("EGP 1,250.00"))
        assertEquals(125000L, CurrencyUtils.parseAmountToMinor("١٢٥٠٫٠٠"))
        assertEquals(null, CurrencyUtils.parseAmountToMinor(""))
    }

    @Test
    fun thousandsSeparatorsAreNotDecimals() {
        assertEquals(125000L, CurrencyUtils.parseAmountToMinor("1,250"))
        assertEquals(1250000L, CurrencyUtils.parseAmountToMinor("١٢٬٥٠٠"))
        assertEquals(1250000L, CurrencyUtils.parseAmountToMinor("12,500"))
        assertEquals(125000L, CurrencyUtils.parseAmountToMinor("1.250,00"))
        assertEquals(125000000L, CurrencyUtils.parseAmountToMinor("1,250,000"))
        assertEquals(1250L, CurrencyUtils.parseAmountToMinor("12.50"))
        assertEquals(350L, CurrencyUtils.parseAmountToMinor("3.50"))
        assertEquals(100L, CurrencyUtils.parseAmountToMinor("1,"))
        assertEquals(null, CurrencyUtils.parseAmountToMinor(","))
    }

    @Test
    fun bankSmsWithArabicDigitsIsNotDropped() {
        assertTrue(BankRuleCatalog.looksTransactional("خصم ١٢٥٠٫٠٠ ج.م من بطاقتك لدى كارفور"))
        assertTrue(BankRuleCatalog.looksTransactional("تم ارسال تحويل 500 جنيه"))
        assertFalse(BankRuleCatalog.looksTransactional("Hello, three branches open"))
        assertFalse(BankRuleCatalog.looksTransactional("3"))
    }

    @Test
    fun referenceIsExtractedAndPrioritizedInDedupe() {
        val body = "CIB: Purchase of EGP 1,250.00 at CARREFOUR on 15/11/2026 14:30 was approved. Ref 123456"
        val result = parser.parse(body)
        assertTrue(result is ParserResult.Success)
        val tx = (result as ParserResult.Success).transaction
        assertEquals("123456", tx.reference)

        val t = 1_700_000_000_000L
        // Same reference in a different 5-minute bucket must still dedupe.
        assertEquals(
            DeduplicationEngine.hashOf(tx.amountMinor, "CARREFOUR", t, tx.reference),
            DeduplicationEngine.hashOf(tx.amountMinor, "Carrefour", t + 30 * 60_000L, tx.reference)
        )
        // Different reference must not dedupe even with same merchant/amount/bucket.
        assertTrue(
            DeduplicationEngine.hashOf(tx.amountMinor, "Carrefour", t, "123456") !=
                DeduplicationEngine.hashOf(tx.amountMinor, "Carrefour", t, "654321")
        )
    }

    @Test
    fun merchantPhrasingVarianceSharesHash() {
        val t = 1_700_000_000_000L
        assertEquals(
            DeduplicationEngine.hashOf(125000L, "CARREFOUR HYPERMARKET", t),
            DeduplicationEngine.hashOf(125000L, "Carrefour", t)
        )
        assertEquals(
            DeduplicationEngine.hashOf(125000L, "كارفور هايبر ماركت", t),
            DeduplicationEngine.hashOf(125000L, "كارفور", t)
        )
        assertTrue(DeduplicationEngine.merchantsMatch("CARREFOUR HYPERMARKET", "Carrefour"))
    }
}
