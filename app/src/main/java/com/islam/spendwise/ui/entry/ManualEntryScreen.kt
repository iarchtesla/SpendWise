package com.islam.spendwise.ui.entry

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islam.spendwise.data.local.SpendWiseDatabaseProvider
import com.islam.spendwise.data.local.entity.TransactionEntity
import com.islam.spendwise.ui.common.currentMonthKey
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ManualEntryScreen() {
    val db = SpendWiseDatabaseProvider.database
    val categories = db.categoryDao().observeAll().collectAsState(initial = emptyList()).value
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var amountText by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var isDebit by remember { mutableStateOf(true) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Add expense",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "All amounts are in Egyptian pounds (EGP / ج.م)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amountText,
            onValueChange = {
                amountText = it
                error = null
            },
            label = { Text("Amount (EGP)") },
            placeholder = { Text("e.g. 250.00") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = merchant,
            onValueChange = { merchant = it },
            label = { Text("Merchant / description") },
            placeholder = { Text("e.g. Carrefour, Uber, Vodafone bill…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = isDebit,
                onClick = { isDebit = true },
                label = { Text("Expense") },
                colors = FilterChipDefaults.filterChipColors()
            )
            FilterChip(
                selected = !isDebit,
                onClick = { isDebit = false },
                label = { Text("Income") },
                colors = FilterChipDefaults.filterChipColors()
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Category",
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategoryId == category.id,
                    onClick = { selectedCategoryId = category.id },
                    label = { Text("${category.icon.orEmpty()} ${category.nameEn}") },
                    colors = FilterChipDefaults.filterChipColors()
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val parsed = amountText.replace(",", ".").trim().toDoubleOrNull()
                if (parsed == null || parsed <= 0.0) {
                    error = "Please enter a valid positive amount."
                    return@Button
                }
                scope.launch {
                    db.transactionDao().insert(
                        TransactionEntity(
                            amountMinor = Math.round(parsed * 100.0),
                            categoryId = selectedCategoryId,
                            merchant = merchant.trim().ifBlank { null },
                            source = "MANUAL",
                            type = if (isDebit) "DEBIT" else "CREDIT",
                            occurredAt = System.currentTimeMillis(),
                            rawMessage = null,
                            sender = null,
                            isReviewed = true,
                            isUserCorrected = false,
                            dedupeHash = UUID.randomUUID().toString()
                        )
                    )
                    amountText = ""
                    merchant = ""
                    isDebit = true
                    selectedCategoryId = null
                    Toast.makeText(context, "Saved — month ${currentMonthKey()}", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save transaction")
        }
    }
}
