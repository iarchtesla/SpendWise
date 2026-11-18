package com.islam.spendwise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_rules")
data class CategoryRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    val keyword: String,
    val isRegex: Boolean = false,
    val priority: Int = 100,
    val isEnabled: Boolean = true
)