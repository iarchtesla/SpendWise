package com.islam.spendwise.data.local.dao

import androidx.room.*
import com.islam.spendwise.data.local.entity.CategoryRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: CategoryRuleEntity): Long

    @Query("SELECT * FROM category_rules WHERE isEnabled = 1 ORDER BY priority ASC, id ASC")
    suspend fun getEnabledOrdered(): List<CategoryRuleEntity>

    @Query("SELECT * FROM category_rules WHERE isEnabled = 1 ORDER BY priority ASC, id ASC")
    fun observeEnabled(): Flow<List<CategoryRuleEntity>>

    @Delete
    suspend fun delete(rule: CategoryRuleEntity)
}