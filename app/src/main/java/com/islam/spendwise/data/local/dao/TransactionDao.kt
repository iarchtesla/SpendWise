package com.islam.spendwise.data.local.dao

import androidx.room.*
import com.islam.spendwise.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions ORDER BY occurredAt DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("SELECT SUM(amountMinor) FROM transactions WHERE type = 'DEBIT' AND occurredAt >= :from AND occurredAt < :to")
    suspend fun totalSpentBetween(from: Long, to: Long): Long?
}