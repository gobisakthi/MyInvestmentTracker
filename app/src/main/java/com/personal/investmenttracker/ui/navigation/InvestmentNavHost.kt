package com.personal.investmenttracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.personal.investmenttracker.ui.account.AccountDetailScreen
import com.personal.investmenttracker.ui.account.AddEditAccountScreen
import com.personal.investmenttracker.ui.asof.AsOfDateEffect
import com.personal.investmenttracker.ui.asof.AsOfDateViewModel
import com.personal.investmenttracker.ui.dashboard.DashboardScreen
import com.personal.investmenttracker.ui.dashboard.DashboardViewModel
import com.personal.investmenttracker.ui.rates.InterestRateHistoryScreen
import com.personal.investmenttracker.ui.transaction.AddTransactionScreen

@Composable
fun InvestmentNavHost(
    modifier: Modifier = Modifier,
    asOfDateViewModel: AsOfDateViewModel = viewModel(),
    dashboardViewModel: DashboardViewModel = viewModel(),
) {
    AsOfDateEffect(asOfDateViewModel)
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Dashboard,
        modifier = modifier,
    ) {
        composable(Routes.Dashboard) {
            DashboardScreen(
                dashboardViewModel = dashboardViewModel,
                asOfDateViewModel = asOfDateViewModel,
                onAddAccount = { navController.navigate(Routes.AddAccount) },
                onAccountClick = { accountId ->
                    navController.navigate(Routes.accountDetail(accountId))
                },
            )
        }
        composable(Routes.AddAccount) {
            AddEditAccountScreen(
                accountId = null,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.EditAccount,
            arguments = listOf(navArgument("accountId") { type = NavType.LongType }),
        ) { entry ->
            AddEditAccountScreen(
                accountId = entry.arguments?.getLong("accountId"),
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.AccountDetail,
            arguments = listOf(navArgument("accountId") { type = NavType.LongType }),
        ) { entry ->
            val accountId = entry.arguments?.getLong("accountId") ?: return@composable
            AccountDetailScreen(
                accountId = accountId,
                asOfDateViewModel = asOfDateViewModel,
                onNavigateBack = { navController.popBackStack() },
                onEditAccount = { navController.navigate(Routes.editAccount(accountId)) },
                onAddTransaction = { navController.navigate(Routes.addTransaction(accountId)) },
                onInterestRateHistory = { navController.navigate(Routes.interestRateHistory(accountId)) },
            )
        }
        composable(
            route = Routes.AddTransaction,
            arguments = listOf(navArgument("accountId") { type = NavType.LongType }),
        ) { entry ->
            AddTransactionScreen(
                accountId = entry.arguments?.getLong("accountId"),
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.InterestRateHistory,
            arguments = listOf(navArgument("accountId") { type = NavType.LongType }),
        ) { entry ->
            InterestRateHistoryScreen(
                accountId = entry.arguments?.getLong("accountId"),
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
