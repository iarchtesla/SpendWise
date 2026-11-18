package com.islam.spendwise.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["dedupeHash"], unique = true)]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amountMinor: Long,
    val currency: String = "EGP",
    val categoryId: Long?,
    val merchant: String?,
    val source: String,
    val type: String,
    val occurredAt: Long,
    val rawMessage: String?,
    val sender: String?,
    val isReviewed: Boolean = false,
    val isUserCorrected: Boolean = false,
    val dedupeHash: String,
    val createdAt: Long = System.currentTimeMillis()
)