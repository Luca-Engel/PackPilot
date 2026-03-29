package com.github.lucaengel.packpilot.ui.screens.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(viewModel: PackingViewModel, onTripCreated: () -> Unit, onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var selectedListId by remember { mutableStateOf("") }
    var selectedTemplateId by remember { mutableStateOf<String?>(null) }
    var useTemplate by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var maxDaysBetweenWashes by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val dateRangePickerState = rememberDateRangePickerState()
    val lists by viewModel.lists.collectAsState()
    val templates by viewModel.templates.collectAsState()

    val filteredLists = lists.values.filter { !it.isGeneral && it.title.contains(searchQuery, ignoreCase = true) }

    // Templates grouped by trip type: named groups sorted alphabetically, ungrouped last
    val templateGroups = run {
        val grouped = templates.values.groupBy { it.tripTypeId }.entries.toList()
        val (named, unnamed) = grouped.partition { it.key != null }
        named.sortedBy { lists[it.key]?.title?.lowercase() } + unnamed
    }

    val datesSelected = dateRangePickerState.selectedStartDateMillis != null &&
        dateRangePickerState.selectedEndDateMillis != null
    val confirmEnabled = title.isNotEmpty() && datesSelected &&
        if (useTemplate) selectedTemplateId != null else selectedListId.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plan New Trip") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val start = Instant.fromEpochMilliseconds(dateRangePickerState.selectedStartDateMillis!!).toLocalDateTime(TimeZone.UTC).date
                        val end = Instant.fromEpochMilliseconds(dateRangePickerState.selectedEndDateMillis!!).toLocalDateTime(TimeZone.UTC).date
                        val laundryDays = maxDaysBetweenWashes.toIntOrNull()
                        val listId = if (useTemplate) templates[selectedTemplateId!!]?.tripTypeId ?: "" else selectedListId
                        val templateId = if (useTemplate) selectedTemplateId else null
                        viewModel.createTrip(title, listId, start, end, laundryDays, templateId)
                        onTripCreated()
                    },
                    modifier = Modifier.padding(16.dp).fillMaxWidth().height(56.dp).testTag("ConfirmTripButton"),
                    enabled = confirmEnabled,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Confirm Trip")
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Trip Name") },
                    modifier = Modifier.fillMaxWidth().testTag("TripNameInput"),
                    shape = RoundedCornerShape(12.dp),
                )
            }

            item {
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth().testTag("DateSelectorCard"),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            if (datesSelected) {
                                val start = Instant.fromEpochMilliseconds(dateRangePickerState.selectedStartDateMillis!!).toLocalDateTime(TimeZone.UTC).date
                                val end = Instant.fromEpochMilliseconds(dateRangePickerState.selectedEndDateMillis!!).toLocalDateTime(TimeZone.UTC).date
                                "$start to $end"
                            } else {
                                "Select Trip Dates"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = maxDaysBetweenWashes,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            // Reject "0" (or "00", etc.) as valid input to ensure positive values only
                            if (it.isNotEmpty() && it.toLongOrNull() == 0L) return@OutlinedTextField
                            maxDaysBetweenWashes = it
                        }
                    },
                    label = { Text("Max days between washes (optional)") },
                    placeholder = { Text("e.g. 7") },
                    modifier = Modifier.fillMaxWidth().testTag("MaxDaysBetweenWashesInput"),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide(); focusManager.clearFocus() }),
                    leadingIcon = { Icon(Icons.Default.LocalLaundryService, null) },
                )
            }

            // Mode selector — only shown when the user has at least one saved template
            if (templates.isNotEmpty()) {
                item {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = !useTemplate,
                            onClick = { useTemplate = false; selectedTemplateId = null },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            modifier = Modifier.testTag("StartFromScratchButton"),
                        ) { Text("Start from scratch") }
                        SegmentedButton(
                            selected = useTemplate,
                            onClick = { useTemplate = true; selectedListId = "" },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            modifier = Modifier.testTag("UseTemplateButton"),
                        ) { Text("Use a template") }
                    }
                }
            }

            if (useTemplate) {
                // Templates grouped by trip type
                templateGroups.forEach { (tripTypeId, groupTemplates) ->
                    item {
                        val groupTitle = tripTypeId?.let { lists[it]?.title } ?: "Other"
                        Text(
                            groupTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("TemplateGroupHeader_$groupTitle"),
                        )
                    }
                    items(groupTemplates.sortedBy { it.name }) { template ->
                        val isSelected = selectedTemplateId == template.id
                        Surface(
                            onClick = { selectedTemplateId = if (isSelected) null else template.id },
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("TemplateOption_${template.name}"),
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedTemplateId = if (isSelected) null else template.id },
                                    modifier = Modifier.testTag("TemplateRadioButton_${template.name}"),
                                )
                                Text(template.name, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            } else {
                item {
                    Text("Activity Type", style = MaterialTheme.typography.titleMedium)
                }
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search activity type...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth().testTag("ActivitySearchInput"),
                        shape = RoundedCornerShape(12.dp),
                    )
                }
                items(filteredLists) { list ->
                    val isSelected = selectedListId == list.id
                    Surface(
                        onClick = { selectedListId = list.id },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("ActivityType_${list.title}"),
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedListId = list.id },
                                modifier = Modifier.testTag("ActivityRadioButton_${list.title}"),
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
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }, modifier = Modifier.testTag("DatePickerOk")) {
                    Text("OK")
                }
            },
        ) {
            DateRangePicker(state = dateRangePickerState, modifier = Modifier.weight(1f))
        }
    }
}
