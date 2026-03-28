package com.github.lucaengel.packpilot.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.lucaengel.packpilot.model.Trip
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PackingViewModel,
    onCreateTrip: () -> Unit,
    onSelectTrip: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    onReviewTrip: (String) -> Unit = {},
) {
    val plannedTrips by viewModel.getPlannedTrips().collectAsState(emptyList())
    val pastTrips by viewModel.getPastTrips().collectAsState(emptyList())
    val tripsAwaitingReview by viewModel.getTripsAwaitingReview().collectAsState(emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PackPilot", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Open navigation drawer")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTrip,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Trip")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My Trips", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (tripsAwaitingReview.isNotEmpty()) {
                    item {
                        Text(
                            "Review Needed",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    items(tripsAwaitingReview, key = { it.id }) { trip ->
                        ReviewPromptCard(trip, onReview = { onReviewTrip(trip.id) })
                    }
                }

                if (plannedTrips.isNotEmpty()) {
                    items(plannedTrips) { trip ->
                        TripCard(trip, onClick = { onSelectTrip(trip.id) })
                    }
                }

                if (pastTrips.isNotEmpty()) {
                    item {
                        Text("Past Trips", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 8.dp))
                    }
                    items(pastTrips) { trip ->
                        TripCard(trip, isPast = true, onClick = { onSelectTrip(trip.id) })
                    }
                }
                
                if (plannedTrips.isEmpty() && pastTrips.isEmpty()) {
                    item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No trips planned yet. Tap + to start!", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewPromptCard(
    trip: Trip,
    onReview: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("ReviewPromptCard_${trip.title}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.RateReview,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(end = 12.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    trip.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                    "Your trip ended — ready to review?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
            TextButton(onClick = onReview) {
                Text("Review", color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
fun TripCard(trip: Trip, isPast: Boolean = false, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.testTag("TripCard_${trip.title}"),
        shape = RoundedCornerShape(20.dp),
        colors = if (isPast) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) else CardDefaults.cardColors()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(trip.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(trip.activityTitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text("${trip.startDate} \u2022 ${trip.days} days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
