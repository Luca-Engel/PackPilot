package com.github.lucaengel.packpilot.model

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable

enum class ItemCategory {
    CLOTHING,
    TOILETRIES,
    ELECTRONICS,
    DOCUMENTS,
    FOOD,
    EQUIPMENT,
    OTHER;

    val displayName: String
        get() = name.lowercase().replaceFirstChar { it.uppercase() }
}

@Serializable
data class PackingItem(
    val id: String,
    val name: String,
    val baseQuantity: Int = 1,
    val isPerDay: Boolean = false,
    val quantityPerDays: Int = 1,
    val category: ItemCategory = ItemCategory.OTHER,
)

@Serializable
data class PackingList(
    val id: String,
    val title: String,
    val itemIds: List<String> = emptyList(),
    val isGeneral: Boolean = false,
)

enum class ItemSource {
    ESSENTIAL,
    ACTIVITY,
    CUSTOM,
}

@Serializable
data class TripItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val isPacked: Boolean = false,
    val originalItemId: String? = null,
    val source: ItemSource = ItemSource.CUSTOM,
    val category: ItemCategory = ItemCategory.OTHER,
)

@Serializable
data class Trip(
    val id: String,
    val title: String,
    val startDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val endDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val items: List<TripItem> = emptyList(),
    val baseListId: String? = null,
    val activityTitle: String = "",
) {
    val days: Int get() = (endDate.toEpochDays() - startDate.toEpochDays() + 1).coerceAtLeast(1)
}

data class SourceSection(
    val source: ItemSource,
    val categories: List<CategorySection>
)

data class CategorySection(
    val category: ItemCategory,
    val items: List<TripItem>
)
