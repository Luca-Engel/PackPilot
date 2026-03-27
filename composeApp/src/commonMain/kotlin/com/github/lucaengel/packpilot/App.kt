package com.github.lucaengel.packpilot

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.github.lucaengel.packpilot.ui.navigation.CreateTripRoute
import com.github.lucaengel.packpilot.ui.navigation.EssentialsRoute
import com.github.lucaengel.packpilot.ui.navigation.HomeRoute
import com.github.lucaengel.packpilot.ui.navigation.TripDetailsRoute
import com.github.lucaengel.packpilot.ui.navigation.TripTypesRoute
import com.github.lucaengel.packpilot.ui.screens.create.CreateTripScreen
import com.github.lucaengel.packpilot.ui.screens.details.TripDetailsScreen
import com.github.lucaengel.packpilot.ui.screens.essentials.GeneralItemsScreen
import com.github.lucaengel.packpilot.ui.screens.home.HomeScreen
import com.github.lucaengel.packpilot.ui.screens.types.ManageTripTypesScreen
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel
import kotlinx.coroutines.launch

@Composable
fun App(viewModel: PackingViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentDestination by navController.currentBackStackEntryAsState()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        Text(
                            "PackPilot",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        HorizontalDivider()
                        NavigationDrawerItem(
                            label = { Text("Packing Lists") },
                            selected = currentDestination?.destination?.hasRoute(HomeRoute::class) == true,
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(HomeRoute) {
                                    popUpTo(HomeRoute) { inclusive = true }
                                }
                            },
                        )
                        NavigationDrawerItem(
                            label = { Text("Essential Items") },
                            selected = currentDestination?.destination?.hasRoute(EssentialsRoute::class) == true,
                            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(EssentialsRoute)
                            },
                        )
                        NavigationDrawerItem(
                            label = { Text("Trip Types") },
                            selected = currentDestination?.destination?.hasRoute(TripTypesRoute::class) == true,
                            icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = null) },
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(TripTypesRoute)
                            },
                        )
                    }
                },
            ) {
                NavHost(navController = navController, startDestination = HomeRoute) {
                    composable<HomeRoute> {
                        HomeScreen(
                            viewModel = viewModel,
                            onCreateTrip = { navController.navigate(CreateTripRoute) },
                            onSelectTrip = { id -> navController.navigate(TripDetailsRoute(id)) },
                            onOpenDrawer = { scope.launch { drawerState.open() } },
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
}
