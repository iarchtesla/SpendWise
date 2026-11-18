package com.islam.spendwise.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.islam.spendwise.R
import com.islam.spendwise.ui.budgets.BudgetsScreen
import com.islam.spendwise.ui.dashboard.DashboardScreen
import com.islam.spendwise.ui.entry.ManualEntryScreen
import com.islam.spendwise.ui.reports.ReportsScreen
import com.islam.spendwise.ui.rules.RulesScreen
import com.islam.spendwise.ui.settings.SettingsScreen
import com.islam.spendwise.ui.transactions.TransactionListScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val TRANSACTIONS = "transactions"
    const val ENTRY = "entry"
    const val REPORTS = "reports"
    const val BUDGETS = "budgets"
    const val RULES = "rules"
    const val SETTINGS = "settings"
}

private data class NavItem(val route: String, val labelRes: Int, val icon: ImageVector)

private val BOTTOM_ITEMS = listOf(
    NavItem(Routes.DASHBOARD, R.string.nav_home, Icons.Filled.Home),
    NavItem(Routes.TRANSACTIONS, R.string.nav_list, Icons.Filled.Receipt),
    NavItem(Routes.ENTRY, R.string.nav_add, Icons.Filled.Add),
    NavItem(Routes.REPORTS, R.string.nav_reports, Icons.Filled.PieChart),
    NavItem(Routes.BUDGETS, R.string.nav_budgets, Icons.Filled.AccountBalance)
)

@Composable
fun SpendWiseNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    fun navigateTab(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.DASHBOARD) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                BOTTOM_ITEMS.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = { navigateTab(item.route) },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(stringResource(item.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(onOpenSettings = { navController.navigate(Routes.SETTINGS) })
            }
            composable(Routes.TRANSACTIONS) { TransactionListScreen() }
            composable(Routes.ENTRY) { ManualEntryScreen() }
            composable(Routes.REPORTS) { ReportsScreen() }
            composable(Routes.BUDGETS) { BudgetsScreen() }
            composable(Routes.RULES) { RulesScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenRules = { navController.navigate(Routes.RULES) }
                )
            }
        }
    }
}
