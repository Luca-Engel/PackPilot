package com.github.lucaengel.packpilot

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.github.lucaengel.packpilot.ui.navigation.*
import com.github.lucaengel.packpilot.ui.screens.create.CreateTripScreen
import com.github.lucaengel.packpilot.ui.screens.details.TripDetailsScreen
import com.github.lucaengel.packpilot.ui.screens.essentials.GeneralItemsScreen
import com.github.lucaengel.packpilot.ui.screens.home.HomeScreen
import com.github.lucaengel.packpilot.ui.screens.types.ManageTripTypesScreen
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel

@Composable
fun App(viewModel: PackingViewModel) {
    val navController = rememberNavController()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            NavHost(navController = navController, startDestination = HomeRoute) {
                composable<HomeRoute> {
                    HomeScreen(
                        viewModel = viewModel,
                        onCreateTrip = { navController.navigate(CreateTripRoute) },
                        onSelectTrip = { id -> navController.navigate(TripDetailsRoute(id)) },
                        onOpenGeneral = { navController.navigate(EssentialsRoute) },
                        onManageTypes = { navController.navigate(TripTypesRoute) },
                    )
                }
                composable<CreateTripRoute> {
                    CreateTripScreen(
                        viewModel = viewModel,
                        onTripCreated = {
                            viewModel.clearHistory()
                            navController.navigate(HomeRoute) {
                                popUpTo(HomeRoute) { inclusive = true }
                            }
                        },
                        onBack = {
                            viewModel.clearHistory()
                            navController.popBackStack()
                        },
                    )
                }
                composable<TripDetailsRoute> { backStackEntry ->
                    val args = backStackEntry.toRoute<TripDetailsRoute>()
                    TripDetailsScreen(
                        viewModel = viewModel,
                        tripId = args.tripId,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable<EssentialsRoute> {
                    GeneralItemsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable<TripTypesRoute> {
                    ManageTripTypesScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}
