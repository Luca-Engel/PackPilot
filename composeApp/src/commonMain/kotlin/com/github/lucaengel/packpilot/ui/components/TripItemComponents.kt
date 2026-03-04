package com.github.lucaengel.packpilot.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.lucaengel.packpilot.model.PackingItem
import com.github.lucaengel.packpilot.model.TripItem
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel

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
                    
                    @OptIn(ExperimentalMaterial3Api::class)
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
