package com.islam.spendwise.service

import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.islam.spendwise.parser.BankRuleCatalog

/**
 * Listens for payment notifications from Egyptian banking apps and InstaPay.
 */
class SpendWiseNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        try {
            val pkg = sbn.packageName ?: return
            val text = extractText(sbn)
            if (text.isBlank()) return
            val isInstapay = pkg.contains("instapay", ignoreCase = true)
            if (!BankRuleCatalog.looksTransactional(text) && !isInstapay) return
            TransactionIngestor.ingest(text, pkg, "APP", isInstapay = isInstapay)
        } catch (_: Exception) {
            // Swallow all notification-path errors.
        }
    }

    /** System killed the binding (OEM/memory pressure) — request rebind or ingestion stops forever. */
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        try {
            requestRebind(ComponentName(this, SpendWiseNotificationListener::class.java))
        } catch (_: Exception) {
            // Best effort; some OEMs require the user to re-toggle Notification Access.
        }
    }

    private fun extractText(sbn: StatusBarNotification): String {
        val notification = sbn.notification ?: return ""
        val extras = notification.extras ?: return ""
        val title = extras.getString("android.title") ?: ""
        val text = extras.getString("android.text") ?: ""
        val bigText = extras.getCharSequence("android.bigText")?.toString().orEmpty()
        val lines = listOf(title, text, bigText).filter { it.isNotBlank() }
        return lines.joinToString(" ").replace(Regex("\\s+"), " ").trim()
    }
}