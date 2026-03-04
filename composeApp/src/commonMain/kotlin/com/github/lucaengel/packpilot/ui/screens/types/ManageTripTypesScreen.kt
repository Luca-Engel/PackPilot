package com.github.lucaengel.packpilot.ui.screens.types

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.lucaengel.packpilot.ui.components.BaseTemplateItemRow
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel

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

            Column(modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp)) {
                if (selectedTypeId != null) {
                    val type = lists[selectedTypeId!!]!!
                    val items by viewModel.observeItemsForList(selectedTypeId!!).collectAsState(emptyList())
                    var showAddItemDialog by remember { mutableStateOf(false) }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(type.title, style = MaterialTheme.typography.headlineSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
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
