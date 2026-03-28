package com.github.lucaengel.packpilot.ui.screens.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.github.lucaengel.packpilot.model.FeedbackType
import com.github.lucaengel.packpilot.model.ItemCategory
import com.github.lucaengel.packpilot.model.TripItem
import com.github.lucaengel.packpilot.model.TripItemFeedback
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostTripReviewScreen(
    viewModel: PackingViewModel,
    tripId: String,
    onBack: () -> Unit,
) {
    val trips by viewModel.trips.collectAsState()
    val trip = trips[tripId] ?: return

    // Adjusted quantities for template saving; updated when QUANTITY_WAS_OFF is confirmed
    var adjustedQuantities by remember {
        mutableStateOf(trip.items.associate { it.id to it.quantity })
    }

    // Track persisted feedback per item; initialised from the trip model
    var itemFeedbacks by remember {
        mutableStateOf(trip.itemFeedback.associateBy { it.itemId })
    }

    // An item is considered reviewed as soon as any feedback is selected
    val reviewedItemIds: Set<String> = itemFeedbacks.keys

    val totalItems = trip.items.size
    val reviewedCount = reviewedItemIds.size
    val progress = if (totalItems == 0) 1f else reviewedCount.toFloat() / totalItems

    // Item ID awaiting quantity input for QUANTITY_WAS_OFF feedback
    var quantityDialogItemId by remember { mutableStateOf<String?>(null) }

    var showSaveDialog by remember { mutableStateOf(false) }

    val itemsByCategory = remember(trip.items) {
        trip.items
            .groupBy { it.category }
            .entries
            .sortedBy { ItemCategory.entries.indexOf(it.key) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Review Trip",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            trip.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("ReviewBackButton"),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("SaveAsTemplateFromReviewButton"),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Save as Template")
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
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("ReviewTripSummaryCard"),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            trip.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("ReviewTripSummaryTitle"),
                        )
                        Text(
                            trip.activityTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.DateRange,
                                null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${trip.startDate} to ${trip.endDate} (${trip.days} days)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "$reviewedCount / $totalItems items reviewed",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("ReviewProgressText"),
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ReviewProgressIndicator"),
                    )
                }
                Spacer(Modifier.height(4.dp))
                HorizontalDivider()
            }

            itemsByCategory.forEach { (category, categoryItems) ->
                item {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 4.dp)
                            .testTag("ReviewCategoryHeader_${category.name}"),
                    )
                }
                items(categoryItems) { tripItem ->
                    ReviewItemRow(
                        tripItem = tripItem,
                        isReviewed = tripItem.id in reviewedItemIds,
                        currentFeedback = itemFeedbacks[tripItem.id]?.feedbackType,
                        onFeedbackSelected = { feedbackType ->
                            if (feedbackType == FeedbackType.QUANTITY_WAS_OFF) {
                                quantityDialogItemId = tripItem.id
                            } else {
                                val feedback = TripItemFeedback(
                                    itemId = tripItem.id,
                                    feedbackType = feedbackType,
                                    timestamp = Clock.System.now().toEpochMilliseconds(),
                                )
                                itemFeedbacks = itemFeedbacks + (tripItem.id to feedback)
                                viewModel.saveTripItemFeedback(tripId, feedback)
                            }
                        },
                    )
                }
            }
        }
    }

    // Quantity dialog for QUANTITY_WAS_OFF feedback
    quantityDialogItemId?.let { itemId ->
        val tripItem = trip.items.find { it.id == itemId }
        if (tripItem != null) {
            var quantityInput by remember(itemId) { mutableStateOf("") }
            val focusRequester = remember { FocusRequester() }

            AlertDialog(
                onDismissRequest = { quantityDialogItemId = null },
                title = { Text("Ideal quantity for ${tripItem.name}") },
                text = {
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }
                    TextField(
                        value = quantityInput,
                        onValueChange = { quantityInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Ideal quantity") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val qty = quantityInput.toIntOrNull()
                                if (qty != null && qty >= 1) {
                                    val feedback = TripItemFeedback(
                                        itemId = itemId,
                                        feedbackType = FeedbackType.QUANTITY_WAS_OFF,
                                        suggestedQuantity = qty,
                                        timestamp = Clock.System.now().toEpochMilliseconds(),
                                    )
                                    itemFeedbacks = itemFeedbacks + (itemId to feedback)
                                    adjustedQuantities = adjustedQuantities + (itemId to qty)
                                    viewModel.saveTripItemFeedback(tripId, feedback)
                                    quantityDialogItemId = null
                                }
                            },
                        ),
                        modifier = Modifier
                            .testTag("FeedbackQuantityInput_$itemId")
                            .focusRequester(focusRequester),
                    )
                },
                confirmButton = {
                    val qty = quantityInput.toIntOrNull()
                    TextButton(
                        onClick = {
                            if (qty != null && qty >= 1) {
                                val feedback = TripItemFeedback(
                                    itemId = itemId,
                                    feedbackType = FeedbackType.QUANTITY_WAS_OFF,
                                    suggestedQuantity = qty,
                                    timestamp = Clock.System.now().toEpochMilliseconds(),
                                )
                                itemFeedbacks = itemFeedbacks + (itemId to feedback)
                                adjustedQuantities = adjustedQuantities + (itemId to qty)
                                viewModel.saveTripItemFeedback(tripId, feedback)
                                quantityDialogItemId = null
                            }
                        },
                        enabled = qty != null && qty >= 1,
                        modifier = Modifier.testTag("ConfirmFeedbackQuantity_$itemId"),
                    ) { Text("Confirm") }
                },
                dismissButton = {
                    TextButton(onClick = { quantityDialogItemId = null }) { Text("Cancel") }
                },
            )
        }
    }

    if (showSaveDialog) {
        var templateName by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }

        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save as Template") },
            text = {
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
                TextField(
                    value = templateName,
                    onValueChange = { templateName = it },
                    label = { Text("Template Name") },
                    modifier = Modifier
                        .testTag("ReviewTemplateNameInput")
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (templateName.isNotBlank()) {
                                viewModel.saveReviewedTripAsTemplate(tripId, templateName.trim(), adjustedQuantities)
                                showSaveDialog = false
                            }
                        },
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveReviewedTripAsTemplate(tripId, templateName.trim(), adjustedQuantities)
                        showSaveDialog = false
                    },
                    enabled = templateName.isNotBlank(),
                    modifier = Modifier.testTag("ConfirmSaveReviewTemplate"),
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReviewItemRow(
    tripItem: TripItem,
    isReviewed: Boolean,
    currentFeedback: FeedbackType?,
    onFeedbackSelected: (FeedbackType) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ReviewItemRow_${tripItem.name}_${tripItem.id}"),
        color = if (isReviewed) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        },
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                tripItem.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isReviewed) FontWeight.Normal else FontWeight.Bold,
                color = if (isReviewed) {
                    MaterialTheme.colorScheme.outline
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            Text(
                tripItem.category.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                FeedbackType.entries.forEach { feedbackType ->
                    FilterChip(
                        selected = currentFeedback == feedbackType,
                        onClick = { onFeedbackSelected(feedbackType) },
                        label = {
                            Text(
                                feedbackType.displayName,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        modifier = Modifier.testTag("FeedbackButton_${feedbackType.name}_${tripItem.id}"),
                    )
                }
            }
        }
    }
}
