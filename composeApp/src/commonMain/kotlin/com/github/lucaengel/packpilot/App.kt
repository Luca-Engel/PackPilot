package com.github.lucaengel.packpilot

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.launch

@Composable
fun App(viewModel: PackingViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
                            selected = false,
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
                            selected = false,
                            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(EssentialsRoute)
                            },
                        )
                        NavigationDrawerItem(
                            label = { Text("Trip Types") },
                            selected = false,
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