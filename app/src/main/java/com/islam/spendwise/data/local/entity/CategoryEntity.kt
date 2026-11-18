package com.islam.spendwise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nameEn: String,
    val nameAr: String,
    val icon: String? = null,
    val colorHex: String? = null,
    val isSystem: Boolean = false,
    val sortOrder: Int = 0
)