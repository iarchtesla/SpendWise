package com.islam.spendwise.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.islam.spendwise.parser.BankRuleCatalog

/**
 * Listens for incoming SMS messages that might be bank transaction alerts.
 * Uses goAsync() so the process stays alive until Room finishes the write.
 */
class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val pendingResult = goAsync()
        try {
            val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (parts.isNullOrEmpty()) {
                pendingResult.finish()
                return
            }

            val body = parts.joinToString(separator = "") { it.messageBody ?: "" }
            val sender = parts.firstOrNull()?.originatingAddress

            if (!BankRuleCatalog.isLikelyBankSender(sender)) {
                pendingResult.finish()
                return
            }
            if (!BankRuleCatalog.looksTransactional(body)) {
                pendingResult.finish()
                return
            }

            TransactionIngestor.ingest(body, sender, "SMS") { pendingResult.finish() }
        } catch (_: Exception) {
            // Never crash the system broadcast path.
            pendingResult.finish()
        }
    }
}
