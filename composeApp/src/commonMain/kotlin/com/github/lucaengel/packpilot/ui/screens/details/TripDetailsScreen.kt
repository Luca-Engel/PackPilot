package com.github.lucaengel.packpilot.ui.screens.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.github.lucaengel.packpilot.model.ItemCategory
import com.github.lucaengel.packpilot.model.ItemSource
import com.github.lucaengel.packpilot.ui.components.CategorySelector
import com.github.lucaengel.packpilot.ui.components.ImprovedTripItemRow
import com.github.lucaengel.packpilot.ui.components.SectionHeader
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    viewModel: PackingViewModel,
    tripId: String,
    onBack: () -> Unit,
) {
    // Clear history when leaving screen (handles system back button)
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearHistory()
        }
    }

    val trips by viewModel.trips.collectAsState()
    val trip = trips[tripId] ?: return

    val sections by viewModel.observeTripSections(tripId).collectAsState(initial = emptyList())

    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showAddCustomDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showSaveAsTemplateDialog by remember { mutableStateOf(false) }

    val dateRangePickerState =
        rememberDateRangePickerState(
            initialSelectedStartDateMillis = trip.startDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
            initialSelectedEndDateMillis = trip.endDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
        )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(trip.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            trip.activityTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = canUndo,
                            modifier = Modifier.testTag("UndoButton"),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, "Undo")
                        }
                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = canRedo,
                            modifier = Modifier.testTag("RedoButton"),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Redo, "Redo")
                        }
                        IconButton(onClick = {
                            viewModel.clearHistory()
                            isEditMode = false
                        }, modifier = Modifier.testTag("SaveEditButton")) {
                            Icon(Icons.Default.Check, "Save", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(
                            onClick = { showDiscardDialog = true },
                            modifier = Modifier.testTag("StopEditingButton"),
                        ) {
                            Icon(Icons.Default.Close, "Stop Editing", tint = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        IconButton(onClick = {
                            viewModel.startEditing()
                            isEditMode = true
                        }, modifier = Modifier.testTag("EditModeButton")) {
                            Icon(Icons.Default.Edit, "Edit")
                        }
                        IconButton(
                            onClick = { showSaveAsTemplateDialog = true },
                            modifier = Modifier.testTag("SaveAsTemplateButton"),
                        ) {
                            Icon(Icons.Default.BookmarkAdd, "Save as Template")
                        }
                        IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.testTag("DeleteTripButton")) {
                            Icon(Icons.Default.Delete, "Delete Trip", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (isEditMode) {
                Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { showAddCustomDialog = true },
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("AddCustomItemButton"),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Custom Item")
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.DateRange,
                                null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${trip.startDate} to ${trip.endDate} (${trip.days} days)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                            )

                            if (isEditMode) {
                                IconButton(
                                    onClick = { showDatePicker = true },
                                    modifier =
                                        Modifier
                                            .size(32.dp)
                                            .padding(start = 4.dp)
                                            .testTag("DatePickerButton"),
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        "Change Dates",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }

                    if (isEditMode) {
                        var maxDaysStr by remember(trip.maxDaysBetweenWashes) {
                            mutableStateOf(trip.maxDaysBetweenWashes?.toString() ?: "")
                        }
                        val maxDaysFocusManager = LocalFocusManager.current
                        val maxDaysKeyboardController = LocalSoftwareKeyboardController.current
                        OutlinedTextField(
                            value = maxDaysStr,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) {
                                    if (it.isNotEmpty() && it.toLongOrNull() == 0L) return@OutlinedTextField
                                    maxDaysStr = it
                                    viewModel.updateTripData(
                                        tripId,
                                        trip.title,
                                        trip.startDate,
                                        trip.endDate,
                                        it.toIntOrNull(),
                                    )
                                }
                            },
                            label = { Text("Max days between washes") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("EditMaxDaysInput"),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    maxDaysKeyboardController?.hide()
                                    maxDaysFocusManager.clearFocus()
                                },
                            ),
                            leadingIcon = { Icon(Icons.Default.LocalLaundryService, null) },
                            shape = RoundedCornerShape(12.dp),
                        )
                    } else if (trip.maxDaysBetweenWashes != null) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocalLaundryService,
                                null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Laundry every ${trip.maxDaysBetweenWashes} days",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                HorizontalDivider()
            }

            sections.forEach { sourceSection ->
                item {
                    val title =
                        when (sourceSection.source) {
                            ItemSource.ESSENTIAL -> "Essential Items"
                            ItemSource.ACTIVITY -> "${trip.activityTitle} Items"
                            ItemSource.CUSTOM -> "Added for this trip"
                            ItemSource.MERGED -> "Multiple Sources"
                        }
                    SectionHeader(title)
                }

                sourceSection.categories.forEach { categorySection ->
                    item {
                        Text(
                            text = categorySection.category.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier =
                                Modifier
                                    .padding(top = 8.dp, bottom = 4.dp)
                                    .testTag(
                                        "CategoryHeader_${sourceSection.source.name}_${categorySection.category.name}",
                                    ),
                        )
                    }
                    items(categorySection.items) { item ->
                        ImprovedTripItemRow(item, tripId, viewModel, isEditMode)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    if (dateRangePickerState.selectedStartDateMillis != null &&
                        dateRangePickerState.selectedEndDateMillis != null
                    ) {
                        val start =
                            Instant
                                .fromEpochMilliseconds(
                                    dateRangePickerState.selectedStartDateMillis!!,
                                ).toLocalDateTime(TimeZone.UTC)
                                .date
                        val end =
                            Instant
                                .fromEpochMilliseconds(
                                    dateRangePickerState.selectedEndDateMillis!!,
                                ).toLocalDateTime(TimeZone.UTC)
                                .date
                        viewModel.updateTripDates(tripId, start, end)
                    }
                    showDatePicker = false
                }, modifier = Modifier.testTag("UpdateDatesConfirm")) { Text("Update") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DateRangePicker(state = dateRangePickerState, modifier = Modifier.weight(1f))
        }
    }

    if (showAddCustomDialog) {
        var name by remember { mutableStateOf("") }
        var qty by remember { mutableStateOf("") }
        var category by remember { mutableStateOf(ItemCategory.OTHER) }

        val nameFocusRequester = remember { FocusRequester() }
        val qtyFocusRequester = remember { FocusRequester() }

        AlertDialog(
            onDismissRequest = { showAddCustomDialog = false },
            title = { Text("Add Custom Item") },
            text = {
                val focusManager = LocalFocusManager.current
                val keyboardController = LocalSoftwareKeyboardController.current

                LaunchedEffect(Unit) {
                    nameFocusRequester.requestFocus()
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Item Name") },
                        modifier =
                            Modifier
                                .testTag("CustomItemNameInput")
                                .focusRequester(nameFocusRequester),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions =
                            KeyboardActions(
                                onNext = {
                                    qtyFocusRequester.requestFocus()
                                },
                            ),
                    )
                    TextField(
                        value = qty,
                        onValueChange = {
                            if (it.all { c -> c.isDigit() }) {
                                qty = it
                            }
                        },
                        label = { Text("Quantity") },
                        modifier =
                            Modifier
                                .testTag("CustomItemQtyInput")
                                .focusRequester(qtyFocusRequester),
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                        keyboardActions =
                            KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                },
                            ),
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Category: ", style = MaterialTheme.typography.bodyMedium)
                        CategorySelector(
                            currentCategory = category,
                            onCategorySelected = { category = it },
                            modifier = Modifier.testTag("CustomItemCategorySelector"),
                        )
                    }
                }
            },
            confirmButton = {
                val isNameValid = name.isNotBlank()
                val isQtyValid = qty.isNotEmpty() && (qty.toIntOrNull() ?: 0) > 0

                TextButton(
                    onClick = {
                        viewModel.addCustomItemToTrip(tripId, name, qty.toIntOrNull() ?: 1, category)
                        showAddCustomDialog = false
                    },
                    enabled = isNameValid && isQtyValid,
                    modifier = Modifier.testTag("ConfirmAddCustomItem"),
                ) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddCustomDialog = false }) { Text("Cancel") } },
        )
    }

    if (showSaveAsTemplateDialog) {
        var templateName by remember { mutableStateOf("") }
        val templateNameFocusRequester = remember { FocusRequester() }

        AlertDialog(
            onDismissRequest = { showSaveAsTemplateDialog = false },
            title = { Text("Save as Template") },
            text = {
                LaunchedEffect(Unit) {
                    templateNameFocusRequester.requestFocus()
                }
                TextField(
                    value = templateName,
                    onValueChange = { templateName = it },
                    label = { Text("Template Name") },
                    modifier = Modifier
                        .testTag("TemplateNameInput")
                        .focusRequester(templateNameFocusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveCurrentTripAsTemplate(tripId, templateName.trim())
                        showSaveAsTemplateDialog = false
                    },
                    enabled = templateName.isNotBlank(),
                    modifier = Modifier.testTag("ConfirmSaveAsTemplate"),
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveAsTemplateDialog = false }) { Text("Cancel") }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Trip?") },
            text = { Text("Are you sure you want to delete this trip and its packing progress?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTrip(tripId)
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("ConfirmDeleteTrip"),
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard Changes?") },
            text = { Text("Are you sure you want to discard all your changes?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.discardEdits()
                        isEditMode = false
                        showDiscardDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("ConfirmDiscardEdits"),
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Keep Editing") }
            },
        )
    }
}
