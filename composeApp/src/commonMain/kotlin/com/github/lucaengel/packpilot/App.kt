package com.github.lucaengel.packpilot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.github.lucaengel.packpilot.model.ItemSource
import com.github.lucaengel.packpilot.model.PackingItem
import com.github.lucaengel.packpilot.model.Trip
import com.github.lucaengel.packpilot.model.TripItem
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

// Best Practice: Use Serializable types for type-safe routing
@Serializable object HomeRoute
@Serializable object CreateTripRoute
@Serializable data class TripDetailsRoute(val tripId: String)
@Serializable object EssentialsRoute
@Serializable object TripTypesRoute

@OptIn(ExperimentalMaterial3Api::class)
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
                        onManageTypes = { navController.navigate(TripTypesRoute) }
                    )
                }
                composable<CreateTripRoute> {
                    CreateTripScreen(
                        viewModel = viewModel,
                        onTripCreated = { 
                            navController.navigate(HomeRoute) {
                                popUpTo(HomeRoute) { inclusive = true }
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<TripDetailsRoute> { backStackEntry ->
                    val args = backStackEntry.toRoute<TripDetailsRoute>()
                    TripDetailsScreen(
                        viewModel = viewModel,
                        tripId = args.tripId,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<EssentialsRoute> {
                    GeneralItemsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<TripTypesRoute> {
                    ManageTripTypesScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PackingViewModel,
    onCreateTrip: () -> Unit,
    onSelectTrip: (String) -> Unit,
    onOpenGeneral: () -> Unit,
    onManageTypes: () -> Unit
) {
    val plannedTrips by viewModel.getPlannedTrips().collectAsState(emptyList())
    val pastTrips by viewModel.getPastTrips().collectAsState(emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PackPilot", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = onManageTypes) {
                        Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "Trip Types")
                    }
                    IconButton(onClick = onOpenGeneral) {
                        Icon(Icons.Default.Settings, contentDescription = "Essentials")
                    }
                }
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
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My Trips", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
fun TripCard(trip: Trip, isPast: Boolean = false, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(viewModel: PackingViewModel, onTripCreated: () -> Unit, onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var selectedListId by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val dateRangePickerState = rememberDateRangePickerState()
    val lists by viewModel.lists.collectAsState()
    val filteredLists = lists.values.filter { !it.isGeneral && it.title.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plan New Trip") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val start = Instant.fromEpochMilliseconds(dateRangePickerState.selectedStartDateMillis!!).toLocalDateTime(TimeZone.UTC).date
                        val end = Instant.fromEpochMilliseconds(dateRangePickerState.selectedEndDateMillis!!).toLocalDateTime(TimeZone.UTC).date
                        viewModel.createTrip(title, selectedListId, start, end)
                        onTripCreated()
                    },
                    modifier = Modifier.padding(16.dp).fillMaxWidth().height(56.dp),
                    enabled = title.isNotEmpty() && selectedListId.isNotEmpty() && dateRangePickerState.selectedStartDateMillis != null && dateRangePickerState.selectedEndDateMillis != null,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Confirm Trip")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Trip Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (dateRangePickerState.selectedStartDateMillis != null && dateRangePickerState.selectedEndDateMillis != null) {
                            val start = Instant.fromEpochMilliseconds(dateRangePickerState.selectedStartDateMillis!!).toLocalDateTime(TimeZone.UTC).date
                            val end = Instant.fromEpochMilliseconds(dateRangePickerState.selectedEndDateMillis!!).toLocalDateTime(TimeZone.UTC).date
                            "$start to $end"
                        } else "Select Trip Dates",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Text("Activity Type", style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search activity type...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredLists) { list ->
                    val isSelected = selectedListId == list.id
                    Surface(
                        onClick = { selectedListId = list.id },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = isSelected, onClick = { selectedListId = list.id })
                            Text(list.title, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("OK") } }
        ) {
            DateRangePicker(state = dateRangePickerState, modifier = Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(viewModel: PackingViewModel, tripId: String, onBack: () -> Unit) {
    val trips by viewModel.trips.collectAsState()
    val trip = trips[tripId] ?: return
    
    val essentialItems = trip.items.filter { it.source == ItemSource.ESSENTIAL }
    val activityItems = trip.items.filter { it.source == ItemSource.ACTIVITY }
    val customItems = trip.items.filter { it.source == ItemSource.CUSTOM }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddCustomDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = trip.startDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
        initialSelectedEndDateMillis = trip.endDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(trip.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(trip.activityTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.EditCalendar, null)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { showAddCustomDialog = true },
                    modifier = Modifier.padding(16.dp).fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Custom Item")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${trip.startDate} to ${trip.endDate} (${trip.days} days)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                HorizontalDivider()
            }

            if (essentialItems.isNotEmpty()) {
                item { SectionHeader("Essential Clothes") }
                items(essentialItems) { item -> ImprovedTripItemRow(item, tripId, viewModel) }
            }

            if (activityItems.isNotEmpty()) {
                item { SectionHeader("${trip.activityTitle} Items") }
                items(activityItems) { item -> ImprovedTripItemRow(item, tripId, viewModel) }
            }

            if (customItems.isNotEmpty()) {
                item { SectionHeader("Added for this trip") }
                items(customItems) { item -> ImprovedTripItemRow(item, tripId, viewModel) }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { 
                TextButton(onClick = {
                    if (dateRangePickerState.selectedStartDateMillis != null && dateRangePickerState.selectedEndDateMillis != null) {
                        val start = Instant.fromEpochMilliseconds(dateRangePickerState.selectedStartDateMillis!!).toLocalDateTime(TimeZone.UTC).date
                        val end = Instant.fromEpochMilliseconds(dateRangePickerState.selectedEndDateMillis!!).toLocalDateTime(TimeZone.UTC).date
                        viewModel.updateTripDates(tripId, start, end)
                    }
                    showDatePicker = false
                }) { Text("Update") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DateRangePicker(state = dateRangePickerState, modifier = Modifier.weight(1f))
        }
    }

    if (showAddCustomDialog) {
        var name by remember { mutableStateOf("") }
        var qty by remember { mutableStateOf("1") }
        AlertDialog(
            onDismissRequest = { showAddCustomDialog = false },
            title = { Text("Add Custom Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") })
                    TextField(value = qty, onValueChange = { if (it.all { c -> c.isDigit() }) qty = it }, label = { Text("Quantity") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotEmpty()) {
                        viewModel.addCustomItemToTrip(tripId, name, qty.toIntOrNull() ?: 1)
                        showAddCustomDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddCustomDialog = false }) { Text("Cancel") } }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Trip?") },
            text = { Text("Are you sure you want to delete this trip and its packing progress?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTrip(tripId)
                    onBack()
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun ImprovedTripItemRow(tripItem: TripItem, tripId: String, viewModel: PackingViewModel) {
    val isPacked = tripItem.isPacked
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        color = if (isPacked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
        onClick = { viewModel.togglePacked(tripId, tripItem.id) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = isPacked, onCheckedChange = { viewModel.togglePacked(tripId, tripItem.id) })
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tripItem.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isPacked) FontWeight.Normal else FontWeight.Bold,
                    color = if (isPacked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        "Quantity: ${tripItem.quantity}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            IconButton(onClick = { viewModel.updateTripItemQuantity(tripId, tripItem.id, tripItem.quantity - 1) }) {
                Icon(Icons.Default.Remove, null, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { viewModel.updateTripItemQuantity(tripId, tripItem.id, tripItem.quantity + 1) }) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { viewModel.removeTripItem(tripId, tripItem.id) }) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralItemsScreen(viewModel: PackingViewModel, onBack: () -> Unit) {
    val items by viewModel.observeGeneralItems().collectAsState(emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Essential Clothes") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Essential")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text("These items are automatically added to every trip.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            
            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(items) { item ->
                    BaseTemplateItemRow(
                        item = item,
                        onUpdateQuantity = { viewModel.updateBaseItemQuantity(item.id, it) },
                        onTogglePerDay = { viewModel.toggleBaseItemPerDay(item.id) },
                        onDelete = { viewModel.removeGeneralItem(item.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var qty by remember { mutableStateOf("1") }
        var isPerDay by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New Essential") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") })
                    TextField(value = qty, onValueChange = { if (it.all { c -> c.isDigit() }) qty = it }, label = { Text("Quantity") })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isPerDay, onCheckedChange = { isPerDay = it })
                        Text("Per Day")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotEmpty()) {
                        viewModel.addGeneralItem(name, qty.toIntOrNull() ?: 1, isPerDay)
                        showAddDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun BaseTemplateItemRow(
    item: PackingItem,
    onUpdateQuantity: (Int) -> Unit,
    onTogglePerDay: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    IconButton(onClick = { onUpdateQuantity(item.baseQuantity - 1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
                    }
                    Text("${item.baseQuantity}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 8.dp))
                    IconButton(onClick = { onUpdateQuantity(item.baseQuantity + 1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    }
                    
                    Spacer(Modifier.width(16.dp))
                    
                    FilterChip(
                        selected = item.isPerDay,
                        onClick = onTogglePerDay,
                        label = { Text(if (item.isPerDay) "Per Day" else "Total", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTripTypesScreen(viewModel: PackingViewModel, onBack: () -> Unit) {
    val lists by viewModel.lists.collectAsState()
    val activityTypes = lists.values.filter { !it.isGeneral }
    var selectedTypeId by remember { mutableStateOf<String?>(null) }
    var showAddTypeDialog by remember { mutableStateOf(false) }
    var isSidebarVisible by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip Types") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { isSidebarVisible = !isSidebarVisible }) {
                        Icon(if (isSidebarVisible) Icons.AutoMirrored.Filled.MenuOpen else Icons.Default.Menu, contentDescription = "Toggle Sidebar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTypeDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New Type")
            }
        }
    ) { padding ->
        Row(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Animating the sidebar (Left Bar)
            AnimatedVisibility(
                visible = isSidebarVisible,
                enter = slideInHorizontally(animationSpec = tween(300)) { -it },
                exit = slideOutHorizontally(animationSpec = tween(300)) { -it }
            ) {
                LazyColumn(
                    modifier = Modifier.width(160.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activityTypes) { type ->
                        Card(
                            onClick = { selectedTypeId = type.id },
                            colors = if (selectedTypeId == type.id) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(type.title, modifier = Modifier.padding(12.dp).fillMaxWidth(), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // Right Side: Items for Selected Type
            Column(modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp)) {
                if (selectedTypeId != null) {
                    val type = lists[selectedTypeId!!]!!
                    val items by viewModel.observeItemsForList(selectedTypeId!!).collectAsState(emptyList())
                    var showAddItemDialog by remember { mutableStateOf(false) }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(type.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showAddItemDialog = true }) {
                            Icon(Icons.Default.AddCircle, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    
                    Text("Template items for this trip type:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    
                    Spacer(Modifier.height(12.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(items) { item ->
                            BaseTemplateItemRow(
                                item = item,
                                onUpdateQuantity = { viewModel.updateBaseItemQuantity(item.id, it) },
                                onTogglePerDay = { viewModel.toggleBaseItemPerDay(item.id) },
                                onDelete = { viewModel.removeItemFromTripType(selectedTypeId!!, item.id) }
                            )
                        }
                    }

                    if (showAddItemDialog) {
                        var name by remember { mutableStateOf("") }
                        var qty by remember { mutableStateOf("1") }
                        var isPerDay by remember { mutableStateOf(false) }
                        AlertDialog(
                            onDismissRequest = { showAddItemDialog = false },
                            title = { Text("Add to ${type.title}") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") })
                                    TextField(value = qty, onValueChange = { if (it.all { c -> c.isDigit() }) qty = it }, label = { Text("Quantity") })
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = isPerDay, onCheckedChange = { isPerDay = it })
                                        Text("Per Day")
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    if (name.isNotEmpty()) {
                                        viewModel.addItemToTripType(selectedTypeId!!, name, qty.toIntOrNull() ?: 1, isPerDay)
                                        showAddItemDialog = false
                                    }
                                }) { Text("Add") }
                            },
                            dismissButton = { TextButton(onClick = { showAddItemDialog = false }) { Text("Cancel") } }
                        )
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Select a trip type to manage its items", color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }

    if (showAddTypeDialog) {
        var typeName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddTypeDialog = false },
            title = { Text("New Trip Type") },
            text = { TextField(value = typeName, onValueChange = { typeName = it }, label = { Text("e.g. Skiing") }) },
            confirmButton = {
                TextButton(onClick = {
                    if (typeName.isNotEmpty()) {
                        viewModel.createNewTripType(typeName)
                        showAddTypeDialog = false
                    }
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showAddTypeDialog = false }) { Text("Cancel") } }
        )
    }
}
