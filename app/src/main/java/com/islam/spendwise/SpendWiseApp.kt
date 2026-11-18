package com.islam.spendwise

import android.app.Application
import com.islam.spendwise.data.local.SpendWiseDatabaseProvider
import com.islam.spendwise.data.local.seed.SeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SpendWiseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val db = SeedData.createDatabase(this)
        SpendWiseDatabaseProvider.database = db
        seedIfNeeded(db)
    }

    private fun seedIfNeeded(db: com.islam.spendwise.data.local.SpendWiseDatabase) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val categories = db.categoryDao().getAll()
                if (categories.isEmpty()) {
                    SeedData.getDefaultCategories().forEach { db.categoryDao().insert(it) }
                    val nameToId = db.categoryDao().getAll().associateBy { it.nameEn }.mapValues { it.value.id }
                    SeedData.getDefaultRules(nameToId).forEach { db.categoryRuleDao().insert(it) }
                }
            } catch (_: Exception) { /* seed is best-effort */ }
        }
    }
}