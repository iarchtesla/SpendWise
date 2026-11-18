package com.islam.spendwise.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import com.islam.spendwise.data.local.SpendWiseDatabaseProvider
import com.islam.spendwise.ui.common.TransactionRow
import com.islam.spendwise.ui.common.formatDay
import com.islam.spendwise.ui.common.formatEgp
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun TransactionListScreen() {
    val db = SpendWiseDatabaseProvider.database
    val transactions = db.transactionDao().observeAll().collectAsState(initial = emptyList()).value
    val categories = db.categoryDao().observeAll().collectAsState(initial = emptyList()).value
    val categoryById = categories.associateBy { it.id }
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<Long?>(null) }
    val editing = transactions.firstOrNull { it.id == editingId }

    val filtered = remember(query, transactions) {
        val q = query.trim().lowercase(Locale.ROOT)
        if (q.isBlank()) transactions
        else transactions.filter {
            (it.merchant?.lowercase(Locale.ROOT)?.contains(q) == true) ||
                (it.rawMessage?.lowercase(Locale.ROOT)?.contains(q) == true) ||
                (it.sender?.lowercase(Locale.ROOT)?.contains(q) == true) ||
                (it.amountMinor.toString().contains(q))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "All transactions",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tap any item to re-categorize or delete it.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search") },
            placeholder = { Text("Merchant, sender, amount…") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (filtered.isEmpty()) {
                item {
                    Text(
                        text = if (query.isBlank()) "No transactions yet." else "No transactions match \"$query\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
            items(filtered) { tx ->
                TransactionRow(
                    merchant = tx.merchant.orEmpty(),
                    categoryLabel = tx.categoryId?.let { categoryById[it]?.nameEn } ?: "Uncategorized",
                    amountMinor = tx.amountMinor,
                    isDebit = tx.type == "DEBIT",
                    dateText = formatDay(tx.occurredAt),
                    onClick = { editingId = tx.id }
                )
            }
        }
    }

    if (editing != null) {
        AlertDialog(
            onDismissRequest = { editingId = null },
            title = { Text(editing.merchant.orEmpty().ifBlank { "Transaction" }) },
            text = {
                Column {
                    Text(
                        text = "${if (editing.type == "DEBIT") "-" else "+"}${formatEgp(editing.amountMinor)} · ${formatDay(editing.occurredAt)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Choose the correct category:",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(categories) { category ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = editing.categoryId == category.id,
                                    onClick = {
                                        val updated = editing.copy(
                                            categoryId = category.id,
                                            isUserCorrected = true
                                        )
                                        scope.launch { db.transactionDao().update(updated) }
                                        editingId = null
                                    }
                                )
                                Text(
                                    text = "${category.icon.orEmpty()} ${category.nameEn}",
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { editingId = null }
                ) { Text("Done") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        scope.launch { db.transactionDao().delete(editing) }
                        editingId = null
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            }
        )
    }
}
