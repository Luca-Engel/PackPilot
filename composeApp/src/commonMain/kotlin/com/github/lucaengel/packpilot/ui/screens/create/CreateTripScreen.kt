package com.github.lucaengel.packpilot.ui.screens.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel
import kotlinx.datetime.*

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
                    modifier = Modifier.padding(16.dp).fillMaxWidth().height(56.dp).testTag("ConfirmTripButton"),
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
                modifier = Modifier.fillMaxWidth().testTag("TripNameInput"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth().testTag("DateSelectorCard"),
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
                modifier = Modifier.fillMaxWidth().testTag("ActivitySearchInput"),
                shape = RoundedCornerShape(12.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredLists) { list ->
                    val isSelected = selectedListId == list.id
                    Surface(
                        onClick = { selectedListId = list.id },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("ActivityType_${list.title}")
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedListId = list.id },
                                modifier = Modifier.testTag("ActivityRadioButton_${list.title}")
                            )
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
            confirmButton = { TextButton(onClick = { showDatePicker = false }, modifier = Modifier.testTag("DatePickerOk")) { Text("OK") } }
        ) {
            DateRangePicker(state = dateRangePickerState, modifier = Modifier.weight(1f))
        }
    }
}
