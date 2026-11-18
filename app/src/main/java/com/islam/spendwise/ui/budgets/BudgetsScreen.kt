package com.islam.spendwise.ui.budgets

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islam.spendwise.data.local.SpendWiseDatabaseProvider
import com.islam.spendwise.data.local.entity.BudgetEntity
import com.islam.spendwise.ui.common.currentMonthKey
import com.islam.spendwise.ui.common.currentMonthRange
import com.islam.spendwise.ui.common.formatEgp
import kotlinx.coroutines.launch

@Composable
fun BudgetsScreen() {
    val db = SpendWiseDatabaseProvider.database
    val scope = rememberCoroutineScope()
    val categories = db.categoryDao().observeAll().collectAsState(initial = emptyList()).value
    val transactions = db.transactionDao().observeAll().collectAsState(initial = emptyList()).value
    val monthKey = currentMonthKey()
    val budgets = db.budgetDao().observeForMonth(monthKey).collectAsState(initial = emptyList()).value
    val budgetByCategory = budgets.associateBy { it.categoryId }

    val (monthStart, monthEnd) = currentMonthRange()
    val spentByCategory = transactions
        .filter { it.type == "DEBIT" && it.occurredAt >= monthStart && it.occurredAt < monthEnd }
        .groupBy { it.categoryId }
        .mapValues { (_, txs) -> txs.sumOf { it.amountMinor } }

    val edits = remember { mutableStateMapOf<Long, String>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Budgets",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Set monthly limits per category · $monthKey · progress turns red when exceeded.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (categories.isEmpty()) {
            item { Text("Loading categories…", modifier = Modifier.padding(24.dp)) }
        } else {
            items(categories) { category ->
                val spent = spentByCategory[category.id] ?: 0L
                val budget = budgetByCategory[category.id]
                val limitMinor = budget?.limitMinor ?: 0L
                val progress = if (limitMinor > 0) spent.toFloat() / limitMinor.toFloat() else 0f
                val over = limitMinor > 0 && spent > limitMinor
                val fieldValue = edits[category.id]
                    ?: budget?.let { String.format(java.util.Locale.ROOT, "%.2f", it.limitMinor / 100.0) }
                    ?: ""

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "${category.icon.orEmpty()} ${category.nameEn}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (limitMinor > 0) {
                                "Spent ${formatEgp(spent)} of ${formatEgp(limitMinor)}"
                            } else {
                                "Spent ${formatEgp(spent)} · no limit set"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (limitMinor > 0) {
                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = fieldValue,
                                onValueChange = { edits[category.id] = it },
                                label = { Text("Monthly limit (EGP)") },
                                placeholder = { Text("e.g. 1500.00") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    val raw = edits[category.id]?.replace(",", ".")?.trim()
                                    val parsed = raw?.toDoubleOrNull()
                                    if (parsed != null && parsed >= 0.0) {
                                        val existing = budget
                                        scope.launch {
                                            db.budgetDao().upsert(
                                                BudgetEntity(
                                                    id = existing?.id ?: 0L,
                                                    categoryId = category.id,
                                                    monthKey = monthKey,
                                                    limitMinor = Math.round(parsed * 100.0),
                                                    createdAt = existing?.createdAt ?: System.currentTimeMillis()
                                                )
                                            )
                                        }
                                        edits.remove(category.id)
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = "Save budget")
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.size(8.dp)) }
    }
}
