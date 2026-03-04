package com.github.lucaengel.packpilot.ui.screens.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.lucaengel.packpilot.model.ItemSource
import com.github.lucaengel.packpilot.ui.components.ImprovedTripItemRow
import com.github.lucaengel.packpilot.ui.components.SectionHeader
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

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
