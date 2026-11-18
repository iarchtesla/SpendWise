package com.islam.spendwise.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islam.spendwise.data.local.SpendWiseDatabaseProvider
import com.islam.spendwise.ui.common.currentMonthRange
import com.islam.spendwise.ui.common.formatEgp

@Composable
fun ReportsScreen() {
    val db = SpendWiseDatabaseProvider.database
    val transactions = db.transactionDao().observeAll().collectAsState(initial = emptyList()).value
    val categories = db.categoryDao().observeAll().collectAsState(initial = emptyList()).value
    val categoryById = categories.associateBy { it.id }

    val (monthStart, monthEnd) = currentMonthRange()
    val monthDebits = transactions.filter {
        it.type == "DEBIT" && it.occurredAt >= monthStart && it.occurredAt < monthEnd
    }
    val total = monthDebits.sumOf { it.amountMinor }

    val breakdown = monthDebits
        .groupBy { it.categoryId }
        .map { (categoryId, txs) ->
            CategoryTotal(
                categoryId = categoryId,
                name = categoryId?.let { categoryById[it]?.nameEn } ?: "Uncategorized",
                icon = categoryId?.let { categoryById[it]?.icon } ?: "📦",
                total = txs.sumOf { it.amountMinor }
            )
        }
        .sortedByDescending { it.total }

    val max = breakdown.maxOfOrNull { it.total }?.takeIf { it > 0 } ?: 1L

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Reports",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Category breakdown for the current month (Africa/Cairo).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Total spent",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatEgp(total),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Across ${monthDebits.size} transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (breakdown.isEmpty()) {
            item {
                Text(
                    text = "No debit transactions this month yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            items(breakdown) { entry ->
                val fraction = entry.total.toFloat() / max.toFloat()
                val barColor = categoryColor(entry.categoryId)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row {
                            Text(
                                text = "${entry.icon} ${entry.name}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatEgp(entry.total),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = barColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${((fraction) * 100).toInt()}% of the highest category",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private data class CategoryTotal(
    val categoryId: Long?,
    val name: String,
    val icon: String,
    val total: Long
)

private fun categoryColor(categoryId: Long?): Color {
    val palette = listOf(
        Color(0xFF00696E), Color(0xFF4E6A6B), Color(0xFF7D5260), Color(0xFFB3261E),
        Color(0xFF386A20), Color(0xFF745B00), Color(0xFF4A6572), Color(0xFF6D4C41)
    )
    val index = categoryId?.let { (it % palette.size).toInt() } ?: 0
    return palette[index]
}
