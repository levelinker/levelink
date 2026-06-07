package com.zeroorhunderd.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zeroorhunderd.app.data.AuctionRepositoryFactory
import com.zeroorhunderd.app.data.UserIdProvider
import com.zeroorhunderd.app.data.WalletRepository
import com.zeroorhunderd.app.domain.usecase.AttemptPurchaseUseCase
import com.zeroorhunderd.app.domain.usecase.CalculateCurrentPriceUseCase
import com.zeroorhunderd.app.domain.usecase.CalculateProgressUseCase
import com.zeroorhunderd.app.presentation.AuctionListScreen
import com.zeroorhunderd.app.presentation.AuctionListViewModel
import com.zeroorhunderd.app.presentation.AuctionListViewModelFactory
import com.zeroorhunderd.app.presentation.AuctionScreen
import com.zeroorhunderd.app.presentation.AuctionViewModel
import com.zeroorhunderd.app.presentation.AuctionViewModelFactory
import com.zeroorhunderd.app.ui.theme.ZeroOrHunderdTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZeroOrHunderdTheme {
                AuctionApp()
            }
        }
    }
}

@Composable
private fun AuctionApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val userIdProvider = remember { UserIdProvider(context.applicationContext) }
    val repository = remember { AuctionRepositoryFactory.create(context.applicationContext) }
    val walletRepository = remember { WalletRepository(context.applicationContext) }

    NavHost(navController = navController, startDestination = "auction_list") {
        composable("auction_list") {
            val listViewModel: AuctionListViewModel = viewModel(
                factory = AuctionListViewModelFactory(
                    repository,
                    walletRepository,
                    CalculateCurrentPriceUseCase(),
                    AttemptPurchaseUseCase(repository),
                    userIdProvider
                )
            )
            AuctionListScreen(
                viewModel = listViewModel,
                onAuctionClick = { auctionId ->
                    navController.navigate("auction_detail/$auctionId")
                }
            )
        }
        composable(
            route = "auction_detail/{auctionId}",
            arguments = listOf(navArgument("auctionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val auctionId = backStackEntry.arguments?.getString("auctionId") ?: return@composable
            val detailViewModel: AuctionViewModel = viewModel(
                factory = AuctionViewModelFactory(
                    auctionId = auctionId,
                    calculateCurrentPriceUseCase = CalculateCurrentPriceUseCase(),
                    calculateProgressUseCase = CalculateProgressUseCase(),
                    attemptPurchaseUseCase = AttemptPurchaseUseCase(repository),
                    auctionRepository = repository,
                    walletRepository = walletRepository,
                    userIdProvider = userIdProvider
                )
            )
            AuctionScreen(
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
