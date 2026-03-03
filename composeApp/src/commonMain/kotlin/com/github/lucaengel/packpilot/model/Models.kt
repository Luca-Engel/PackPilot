package com.github.lucaengel.packpilot.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Serializable
data class PackingItem(
    val id: String,
    val name: String,
    val baseQuantity: Int = 1,
    val isPerDay: Boolean = false,
)

@Serializable
data class PackingList(
    val id: String,
    val title: String,
    val itemIds: List<String> = emptyList(),
    val isGeneral: Boolean = false
)

@Serializable
data class TripItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val isPacked: Boolean = false,
    val originalItemId: String? = null
)

@Serializable
data class Trip(
    val id: String,
    val title: String,
    val startDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val endDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val items: List<TripItem> = emptyList(),
    val baseListId: String? = null,
    val activityTitle: String = "" // Added to display in overview
) {
    val days: Int get() = (endDate.toEpochDays() - startDate.toEpochDays() + 1).coerceAtLeast(1)
}
