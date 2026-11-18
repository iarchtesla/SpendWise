package com.islam.spendwise.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 → v2: add a unique index on budgets(categoryId, monthKey).
 * Pre-existing duplicate rows (from the un-indexed upsert path) are collapsed,
 * keeping the oldest row per (categoryId, monthKey), before the unique index is built.
 * Index name matches Room's canonical `index_<table>_<col1>_<col2>` so identity-hash validation passes.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "DELETE FROM budgets WHERE id NOT IN (" +
                "SELECT MIN(id) FROM budgets GROUP BY categoryId, monthKey" +
                ")"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_budgets_categoryId_monthKey` " +
                "ON `budgets` (`categoryId`, `monthKey`)"
        )
    }
}
