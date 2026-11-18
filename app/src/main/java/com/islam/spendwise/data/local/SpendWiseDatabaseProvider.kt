package com.islam.spendwise.data.local

import com.islam.spendwise.data.local.dao.*

/**
 * Simple in-process provider for the Room database.
 * Initialized once by [SpendWiseApp].
 */
object SpendWiseDatabaseProvider {
    lateinit var database: SpendWiseDatabase

    fun init(database: SpendWiseDatabase) {
        this.database = database
    }
}