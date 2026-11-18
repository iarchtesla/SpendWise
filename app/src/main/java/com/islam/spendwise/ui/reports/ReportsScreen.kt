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
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islam.spendwise.R
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
                name = categoryId?.let { categoryById[it]?.nameEn }
                    ?: stringResource(R.string.uncategorized),
                icon = categoryId?.let { categoryById[it]?.icon } ?: "📦",
                total = txs.sumOf { it.amountMinor }
            )
        }
        .sortedByDescending { it.total }

    val max = breakdown.maxOfOrNull { it.total }?.takeIf { it > 0 } ?: 1L

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.reports_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.reports_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.total_spent),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatEgp(total),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = stringResource(R.string.across_transactions, monthDebits.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                    )
                }
            }
        }

        if (breakdown.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.no_debits),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            items(breakdown, key = { it.categoryId ?: -1L }) { entry ->
                val fraction = entry.total.toFloat() / max.toFloat()
                val barColor = categoryColor(entry.categoryId)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = barColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.pct_of_top, (fraction * 100).toInt()),
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

private fun categoryColor(categoryId: Long?): androidx.compose.ui.graphics.Color {
    val palette = listOf(
        androidx.compose.ui.graphics.Color(0xFF00696E), androidx.compose.ui.graphics.Color(0xFF4E6A6B),
        androidx.compose.ui.graphics.Color(0xFF745B00), androidx.compose.ui.graphics.Color(0xFFB3261E),
        androidx.compose.ui.graphics.Color(0xFF386A20), androidx.compose.ui.graphics.Color(0xFF4A6572),
        androidx.compose.ui.graphics.Color(0xFF6D4C41), androidx.compose.ui.graphics.Color(0xFF7D5260)
    )
    val index = categoryId?.let { (it % palette.size).toInt() } ?: 0
    return palette[index]
}
