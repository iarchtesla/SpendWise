package com.islam.spendwise.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.islam.spendwise.data.local.dao.*
import com.islam.spendwise.data.local.entity.*

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class, CategoryRuleEntity::class, BudgetEntity::class],
    version = 2,
    exportSchema = false
)
abstract class SpendWiseDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun categoryRuleDao(): CategoryRuleDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        const val DATABASE_NAME = "spendwise.db"
    }
}