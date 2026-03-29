package com.github.lucaengel.packpilot.model

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable

@Serializable
enum class FeedbackType {
    BROUGHT_AND_NEEDED,
    BROUGHT_BUT_DIDNT_NEED,
    NEEDED_BUT_DIDNT_BRING,
    QUANTITY_WAS_OFF,
    ;

    val displayName: String
        get() = when (this) {
            BROUGHT_AND_NEEDED -> "Brought and needed"
            BROUGHT_BUT_DIDNT_NEED -> "Brought but didn't need"
            NEEDED_BUT_DIDNT_BRING -> "Needed but didn't bring"
            QUANTITY_WAS_OFF -> "Quantity was off"
        }
}

@Serializable
data class TripItemFeedback(
    val itemId: String,
    val feedbackType: FeedbackType,
    // Computed total for the trip (used for template saving)
    val suggestedQuantity: Int? = null,
    // Whether the suggestion is expressed as a rate (e.g. "1 per 2 days")
    val suggestedIsPerDay: Boolean = false,
    val suggestedBaseQuantity: Int? = null,
    val suggestedQuantityPerDays: Int = 1,
    val timestamp: Long = 0L,
)

enum class ItemCategory {
    CLOTHING,
    TOILETRIES,
    ELECTRONICS,
    DOCUMENTS,
    FOOD,
    EQUIPMENT,
    OTHER,
    ;

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
    MERGED,
}

@Serializable
data class TripItemSourceInfo(
    val source: ItemSource,
    val name: String,
    val quantity: Int,
    val originalItemId: String? = null,
    val addedAt: Long = 0L,
    val category: ItemCategory = ItemCategory.OTHER,
)

@Serializable
data class TripItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val isPacked: Boolean = false,
    val sources: List<TripItemSourceInfo> = emptyList(),
    val category: ItemCategory = ItemCategory.OTHER,
)

@Serializable
data class Trip(
    val id: String,
    val title: String,
    val startDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val endDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val items: List<TripItem> = emptyList(),
    val tripTypeId: String? = null,
    val activityTitle: String = "",
    val maxDaysBetweenWashes: Int? = null,
    val isReviewed: Boolean = false,
    val itemFeedback: List<TripItemFeedback> = emptyList(),
) {
    val days: Int get() = (endDate.toEpochDays() - startDate.toEpochDays() + 1).coerceAtLeast(1)
}

data class SourceSection(
    val source: ItemSource,
    val categories: List<CategorySection>,
)

data class CategorySection(
    val category: ItemCategory,
    val items: List<TripItem>,
)

@Serializable
data class TemplateItem(
    val name: String,
    val quantity: Int,
    val category: ItemCategory = ItemCategory.OTHER,
    val source: ItemSource = ItemSource.CUSTOM,
)

@Serializable
data class TripTemplate(
    val id: String,
    val name: String,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val items: List<TemplateItem> = emptyList(),
)
