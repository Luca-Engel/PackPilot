package com.github.lucaengel.packpilot.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.lucaengel.packpilot.model.ItemCategory
import com.github.lucaengel.packpilot.model.PackingItem
import com.github.lucaengel.packpilot.model.TripItem
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel

@Composable
fun ImprovedTripItemRow(
    tripItem: TripItem,
    tripId: String,
    viewModel: PackingViewModel,
    isEditMode: Boolean,
) {
    val isPacked = tripItem.isPacked
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).testTag("TripItemRow_${tripItem.name}"),
        color =
            if (isPacked) {
                MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.4f,
                )
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            },
        onClick = { viewModel.togglePacked(tripId, tripItem.id) },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = isPacked,
                onCheckedChange = { viewModel.togglePacked(tripId, tripItem.id) },
                modifier = Modifier.testTag("PackedCheckbox_${tripItem.name}"),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tripItem.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isPacked) FontWeight.Normal else FontWeight.Bold,
                    color = if (isPacked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Text(
                            "Qty: ${tripItem.quantity}",
                            modifier =
                                Modifier
                                    .padding(
                                        horizontal = 8.dp,
                                        vertical = 2.dp,
                                    ).testTag("ItemQuantity_${tripItem.name}"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                    if (isEditMode) {
                        CategorySelector(
                            currentCategory = tripItem.category,
                            onCategorySelected = { viewModel.updateTripItemCategory(tripId, tripItem.id, it) },
                            modifier =
                                Modifier
                                    .padding(
                                        start = 8.dp,
                                        top = 4.dp,
                                    ).testTag("CategorySelector_${tripItem.name}"),
                        )
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                        ) {
                            Text(
                                tripItem.category.displayName,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        }
                    }
                }
            }

            if (isEditMode) {
                IconButton(
                    onClick = { viewModel.updateTripItemQuantity(tripId, tripItem.id, tripItem.quantity - 1) },
                    modifier = Modifier.testTag("DecreaseQuantity_${tripItem.name}"),
                ) {
                    Icon(Icons.Default.Remove, "Decrease")
                }
                IconButton(
                    onClick = { viewModel.updateTripItemQuantity(tripId, tripItem.id, tripItem.quantity + 1) },
                    modifier = Modifier.testTag("IncreaseQuantity_${tripItem.name}"),
                ) {
                    Icon(Icons.Default.Add, "Increase")
                }
                IconButton(
                    onClick = { viewModel.removeTripItem(tripId, tripItem.id) },
                    modifier = Modifier.testTag("DeleteItem_${tripItem.name}"),
                ) {
                    Icon(
                        Icons.Default.Delete,
                        "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun CategorySelector(
    currentCategory: ItemCategory,
    onCategorySelected: (ItemCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(6.dp),
            onClick = { expanded = true },
            modifier = modifier,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    currentCategory.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            ItemCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.displayName) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun BaseTemplateItemRow(
    item: PackingItem,
    onUpdateQuantity: (Int) -> Unit,
    onTogglePerDay: () -> Unit,
    onUpdateCategory: (ItemCategory) -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().testTag("BaseItemRow_${item.name}"), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    IconButton(
                        onClick = { onUpdateQuantity(item.baseQuantity - 1) },
                        modifier = Modifier.size(32.dp).testTag("DecreaseBaseQty_${item.name}"),
                    ) {
                        Icon(Icons.Default.Remove, "Decrease")
                    }
                    Text(
                        "${item.baseQuantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 4.dp).testTag("BaseQtyText_${item.name}"),
                    )
                    IconButton(
                        onClick = { onUpdateQuantity(item.baseQuantity + 1) },
                        modifier = Modifier.size(32.dp).testTag("IncreaseBaseQty_${item.name}"),
                    ) {
                        Icon(Icons.Default.Add, "Increase")
                    }

                    Spacer(Modifier.width(8.dp))

                    @OptIn(ExperimentalMaterial3Api::class)
                    FilterChip(
                        selected = item.isPerDay,
                        onClick = onTogglePerDay,
                        label = {
                            Text(
                                if (item.isPerDay) "Per Day" else "Total",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        modifier = Modifier.testTag("PerDayChip_${item.name}"),
                    )

                    Spacer(Modifier.width(8.dp))

                    CategorySelector(
                        currentCategory = item.category,
                        onCategorySelected = onUpdateCategory,
                        modifier = Modifier.testTag("BaseCategorySelector_${item.name}"),
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.testTag("DeleteBaseItem_${item.name}")) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}
