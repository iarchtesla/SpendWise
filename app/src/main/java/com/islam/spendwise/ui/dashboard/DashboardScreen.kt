package com.islam.spendwise.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islam.spendwise.data.local.SpendWiseDatabaseProvider
import com.islam.spendwise.ui.common.TransactionRow
import com.islam.spendwise.ui.common.currentMonthRange
import com.islam.spendwise.ui.common.formatDay
import com.islam.spendwise.ui.common.formatEgp
import com.islam.spendwise.ui.onboarding.PermissionRationaleBanner

@Composable
fun DashboardScreen(onOpenRules: () -> Unit) {
    val db = SpendWiseDatabaseProvider.database
    val transactions = db.transactionDao().observeAll().collectAsState(initial = emptyList()).value
    val categories = db.categoryDao().observeAll().collectAsState(initial = emptyList()).value
    val categoryById = categories.associateBy { it.id }

    val (monthStart, monthEnd) = currentMonthRange()
    val monthTx = transactions.filter {
        it.type == "DEBIT" && it.occurredAt >= monthStart && it.occurredAt < monthEnd
    }
    val monthTotal = monthTx.sumOf { it.amountMinor }
    val creditTotal = transactions
        .filter { it.type == "CREDIT" && it.occurredAt >= monthStart && it.occurredAt < monthEnd }
        .sumOf { it.amountMinor }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SpendWise",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Local-first expense tracker · EGP",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onOpenRules) {
                    Icon(Icons.Filled.Settings, contentDescription = "Category rules")
                }
            }
        }

        item {
            PermissionRationaleBanner()
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Spent this month",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatEgp(monthTotal),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Received: ${formatEgp(creditTotal)} · ${monthTx.size} transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = "Auto-captured & manual, all in one place",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Text(
                text = "Recent transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (transactions.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No transactions yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bank SMS and InstaPay notifications will appear here automatically once you grant SMS and Notification Access permissions. Or use the Add tab to create one manually.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(transactions.take(12)) { tx ->
                val categoryName = tx.categoryId?.let { categoryById[it]?.nameEn } ?: "Uncategorized"
                TransactionRow(
                    merchant = tx.merchant.orEmpty(),
                    categoryLabel = categoryName,
                    amountMinor = tx.amountMinor,
                    isDebit = tx.type == "DEBIT",
                    dateText = formatDay(tx.occurredAt)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}
