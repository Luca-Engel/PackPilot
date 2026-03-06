package com.github.lucaengel.packpilot.ui.screens.essentials

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.lucaengel.packpilot.ui.components.BaseTemplateItemRow
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralItemsScreen(
    viewModel: PackingViewModel,
    onBack: () -> Unit,
) {
    // Reset history when screen is closed (including system back button)
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearHistory()
        }
    }

    val items by viewModel.observeGeneralItems().collectAsState(emptyList())
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Essential Clothes") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    if (canUndo || canRedo) {
                        IconButton(onClick = { viewModel.undo() }, enabled = canUndo) {
                            Icon(Icons.AutoMirrored.Filled.Undo, "Undo")
                        }
                        IconButton(onClick = { viewModel.redo() }, enabled = canRedo) {
                            Icon(Icons.AutoMirrored.Filled.Redo, "Redo")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Essential")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                "These items are automatically added to every trip.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items) { item ->
                    BaseTemplateItemRow(
                        item = item,
                        onUpdateQuantity = { viewModel.updateBaseItemQuantity(item.id, it) },
                        onTogglePerDay = { viewModel.toggleBaseItemPerDay(item.id) },
                        onDelete = { viewModel.removeGeneralItem(item.id) },
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
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Item Name") },
                        modifier = Modifier.testTag("EssentialItemNameInput"),
                    )
                    TextField(
                        value = qty,
                        onValueChange = { if (it.all { c -> c.isDigit() }) qty = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.testTag("EssentialItemQtyInput"),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isPerDay,
                            onCheckedChange = { isPerDay = it },
                            modifier = Modifier.testTag("EssentialPerDayCheckbox"),
                        )
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
                }, modifier = Modifier.testTag("ConfirmAddEssential")) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } },
        )
    }
}
