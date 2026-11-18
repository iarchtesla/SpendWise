package com.islam.spendwise.data.local.dao

import androidx.room.*
import com.islam.spendwise.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: BudgetEntity): Long

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND monthKey = :monthKey")
    suspend fun get(categoryId: Long, monthKey: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE monthKey = :monthKey")
    fun observeForMonth(monthKey: String): Flow<List<BudgetEntity>>
}