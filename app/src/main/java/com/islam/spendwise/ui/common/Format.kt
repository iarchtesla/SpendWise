package com.islam.spendwise.ui.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val dayFormat = SimpleDateFormat("dd MMM yyyy", Locale.ROOT)
private val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.ROOT)

fun formatEgp(minor: Long): String =
    "EGP " + String.format(Locale.ROOT, "%,.2f", minor / 100.0)

fun formatDay(epochMs: Long): String = dayFormat.format(Date(epochMs))

fun currentMonthKey(): String = monthKeyFormat.format(Date())

/** Start (inclusive) and end (exclusive) of the current month in epoch millis, Africa/Cairo. */
fun currentMonthRange(): Pair<Long, Long> {
    val cal = java.util.Calendar.getInstance(TimeZone.getTimeZone("Africa/Cairo"))
    val year = cal.get(java.util.Calendar.YEAR)
    val month = cal.get(java.util.Calendar.MONTH)
    cal.clear()
    cal.set(year, month, 1, 0, 0, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    val start = cal.timeInMillis
    cal.add(java.util.Calendar.MONTH, 1)
    val end = cal.timeInMillis
    return start to end
}
