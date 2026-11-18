package com.islam.spendwise.core

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Small date/time helpers for the UI and the database. */
object DateUtils {
    fun monthKeyFor(timestamp: Long, pattern: String = "yyyy-MM"): String {
        return SimpleDateFormat(pattern, Locale.ROOT).format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(timestamp))
    }

    fun formatEgp(minor: Long): String = String.format(Locale.ROOT, "EGP %.2f", minor / 100.0)
}
