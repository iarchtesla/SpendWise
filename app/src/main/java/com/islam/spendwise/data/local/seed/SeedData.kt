package com.islam.spendwise.data.local.seed

import android.content.Context
import androidx.room.Room
import com.islam.spendwise.data.local.SpendWiseDatabase
import com.islam.spendwise.data.local.entity.CategoryEntity
import com.islam.spendwise.data.local.entity.CategoryRuleEntity

/**
 * Default categories and categorization rules for a fresh install.
 */
object SeedData {

    fun getDefaultCategories(): List<CategoryEntity> = listOf(
        CategoryEntity(nameEn = "Groceries", nameAr = "بقالة", icon = "🛒", colorHex = "#FF6B6B", sortOrder = 1),
        CategoryEntity(nameEn = "Transport", nameAr = "مواصلات", icon = "🚗", colorHex = "#4ECDC4", sortOrder = 2),
        CategoryEntity(nameEn = "Restaurants", nameAr = "مطاعم", icon = "🍽️", colorHex = "#F7C873", sortOrder = 3),
        CategoryEntity(nameEn = "Bills & Utilities", nameAr = "فواتير", icon = "🧾", colorHex = "#B197FC", sortOrder = 4),
        CategoryEntity(nameEn = "Health", nameAr = "صحة", icon = "💊", colorHex = "#8CE99A", sortOrder = 5),
        CategoryEntity(nameEn = "Shopping", nameAr = "تسوق", icon = "🛍️", colorHex = "#FFC9C9", sortOrder = 6),
        CategoryEntity(nameEn = "Transfers", nameAr = "تحويلات", icon = "💸", colorHex = "#A5D8FF", sortOrder = 7),
        CategoryEntity(nameEn = "Other", nameAr = "أخرى", icon = "📦", colorHex = "#CED4DA", sortOrder = 99)
    )

    fun getDefaultRules(categoryIdByName: Map<String, Long>): List<CategoryRuleEntity> {
        fun catId(name: String): Long? = categoryIdByName[name]
        return buildList {
            listOf(
                Triple("uber", "Transport", 10),
                Triple("careem", "Transport", 10),
                Triple("metro", "Transport", 10),
                Triple("grocery", "Groceries", 10),
                Triple("supermarket", "Groceries", 10),
                Triple("market", "Groceries", 10),
                Triple("restaurant", "Restaurants", 10),
                Triple("cafe", "Restaurants", 10),
                Triple("coffee", "Restaurants", 10),
                Triple("pharmacy", "Health", 10),
                Triple("hospital", "Health", 10),
                Triple("electric", "Bills & Utilities", 10),
                Triple("water", "Bills & Utilities", 10),
                Triple("instapay transfer", "Transfers", 10),
                Triple("transfer", "Transfers", 10),
                Triple("amazon", "Shopping", 10)
            ).forEach { (keyword, category, priority) ->
                catId(category)?.let {
                    add(CategoryRuleEntity(categoryId = it, keyword = keyword, priority = priority))
                }
            }
        }
    }

    fun createDatabase(context: Context): SpendWiseDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            SpendWiseDatabase::class.java,
            SpendWiseDatabase.DATABASE_NAME
        ).addMigrations(com.islam.spendwise.data.local.MIGRATION_1_2).build()
    }
}