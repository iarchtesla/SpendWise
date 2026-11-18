package com.islam.spendwise.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    indices = [Index(value = ["categoryId", "monthKey"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    val monthKey: String,   // e.g. "2026-11"
    val limitMinor: Long,
    val createdAt: Long = System.currentTimeMillis()
)
