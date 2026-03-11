package com.github.lucaengel.packpilot.viewmodel

import com.github.lucaengel.packpilot.model.*
import com.github.lucaengel.packpilot.repository.PackingRepository
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.math.ceil
import kotlin.random.Random

data class AppState(
    val items: Map<String, PackingItem>,
    val lists: Map<String, PackingList>,
    val trips: Map<String, Trip>,
)

class PackingViewModel(
    private val repository: PackingRepository,
) {
    val items = repository.items
    val lists = repository.lists
    val trips = repository.trips

    // History stacks
    private val undoStack = mutableListOf<AppState>()
    private val redoStack = mutableListOf<AppState>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo = _canRedo.asStateFlow()

    fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
        updateHistoryFlags()
    }

    private fun recordHistory() {
        val currentState = AppState(items.value, lists.value, trips.value)
        undoStack.add(currentState)
        if (undoStack.size > 50) undoStack.removeAt(0) // Limit history size
        redoStack.clear() // New actions break the redo chain
        updateHistoryFlags()
    }

    fun undo() {
        if (undoStack.isEmpty()) return

        // Save current to redo before reverting
        redoStack.add(AppState(items.value, lists.value, trips.value))

        val previousState = undoStack.removeAt(undoStack.size - 1)
        applyState(previousState)
        updateHistoryFlags()
    }

    fun redo() {
        if (redoStack.isEmpty()) return

        // Save current to undo before reapplying
        undoStack.add(AppState(items.value, lists.value, trips.value))

        val nextState = redoStack.removeAt(redoStack.size - 1)
        applyState(nextState)
        updateHistoryFlags()
    }

    private fun applyState(state: AppState) {
        repository.restoreState(state.items, state.lists, state.trips)
    }

    private fun updateHistoryFlags() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    fun calculateQuantity(
        item: PackingItem,
        tripDays: Int,
        maxDaysBetweenWashes: Int? = null,
    ): Int {
        val sanitizedMaxDays = maxDaysBetweenWashes?.takeIf { it > 0 }

        val effectiveDays =
            if (item.category == ItemCategory.CLOTHING && sanitizedMaxDays != null) {
                tripDays.coerceAtMost(sanitizedMaxDays)
            } else {
                tripDays
            }

        val baseQty =
            if (item.isPerDay) {
                ceil(item.baseQuantity.toDouble() * effectiveDays / item.quantityPerDays.coerceAtLeast(1)).toInt()
            } else {
                item.baseQuantity
            }

        return if (
            item.category == ItemCategory.CLOTHING &&
            item.isPerDay && // only need to wash items if they are designated as "per day"
            sanitizedMaxDays != null &&
            tripDays > sanitizedMaxDays
        ) {
            // 1 more item for the day when you wash if you need to wash
            baseQty + 1
        } else {
            baseQty
        }
    }

    fun createTrip(
        title: String,
        listId: String,
        startDate: LocalDate,
        endDate: LocalDate,
        maxDaysBetweenWashes: Int? = null,
    ) {
        recordHistory()
        val tripId = "trip_${Clock.System.now().toEpochMilliseconds()}_${Random.nextInt(1000)}"
        val activityTitle = lists.value[listId]?.title ?: ""
        val tempTrip =
            Trip(
                id = tripId,
                title = title,
                startDate = startDate,
                endDate = endDate,
                activityTitle = activityTitle,
                baseListId = listId,
                maxDaysBetweenWashes = maxDaysBetweenWashes,
            )
        val days = tempTrip.days

        val listItems = repository.getItemsForList(listId)
        val generalItems = repository.getGeneralItems()

        val tripItems = mutableListOf<TripItem>()

        generalItems.forEach { item ->
            val qty = calculateQuantity(item, days, maxDaysBetweenWashes)
            tripItems.add(
                TripItem(
                    id = "${item.id}_${Random.nextInt()}",
                    name = item.name,
                    quantity = qty,
                    originalItemId = item.id,
                    source = ItemSource.ESSENTIAL,
                    category = item.category,
                ),
            )
        }

        listItems.forEach { item ->
            val qty = calculateQuantity(item, days, maxDaysBetweenWashes)
            tripItems.add(
                TripItem(
                    id = "${item.id}_${Random.nextInt()}",
                    name = item.name,
                    quantity = qty,
                    originalItemId = item.id,
                    source = ItemSource.ACTIVITY,
                    category = item.category,
                ),
            )
        }

        repository.addTrip(tempTrip.copy(items = tripItems))
    }

    fun updateTripData(
        tripId: String,
        title: String,
        startDate: LocalDate,
        endDate: LocalDate,
        maxDaysBetweenWashes: Int? = null,
    ) {
        recordHistory()
        val trip = trips.value[tripId] ?: return
        val newDays = (endDate.toEpochDays() - startDate.toEpochDays() + 1).coerceAtLeast(1)

        val itemsMap = items.value
        val updatedItems =
            trip.items.map { item ->
                if (item.source == ItemSource.CUSTOM || item.originalItemId == null) {
                    item
                } else {
                    val baseItem = itemsMap[item.originalItemId]
                    if (baseItem != null) {
                        val newQty = calculateQuantity(baseItem, newDays, maxDaysBetweenWashes)
                        item.copy(quantity = newQty)
                    } else {
                        item
                    }
                }
            }

        repository.updateTrip(
            trip.copy(
                title = title,
                startDate = startDate,
                endDate = endDate,
                items = updatedItems,
                maxDaysBetweenWashes = maxDaysBetweenWashes,
            ),
        )
    }

    fun updateTripDates(
        tripId: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ) {
        val trip = trips.value[tripId] ?: return
        updateTripData(tripId, trip.title, startDate, endDate, trip.maxDaysBetweenWashes)
    }

    fun deleteTrip(tripId: String) {
        recordHistory()
        repository.deleteTrip(tripId)
    }

    fun togglePacked(
        tripId: String,
        tripItemId: String,
    ) {
        val trip = trips.value[tripId] ?: return
        val updatedItems =
            trip.items.map {
                if (it.id == tripItemId) it.copy(isPacked = !it.isPacked) else it
            }
        repository.updateTrip(trip.copy(items = updatedItems))
    }

    fun updateTripItemQuantity(
        tripId: String,
        tripItemId: String,
        newQuantity: Int,
    ) {
        if (newQuantity < 1) return
        recordHistory()
        val trip = trips.value[tripId] ?: return
        val updatedItems =
            trip.items.map {
                if (it.id == tripItemId) it.copy(quantity = newQuantity) else it
            }
        repository.updateTrip(trip.copy(items = updatedItems))
    }

    fun updateTripItemCategory(
        tripId: String,
        tripItemId: String,
        category: ItemCategory,
    ) {
        recordHistory()
        val trip = trips.value[tripId] ?: return
        val updatedItems =
            trip.items.map {
                if (it.id == tripItemId) it.copy(category = category) else it
            }
        repository.updateTrip(trip.copy(items = updatedItems))
    }

    fun removeTripItem(
        tripId: String,
        tripItemId: String,
    ) {
        recordHistory()
        val trip = trips.value[tripId] ?: return
        val updatedItems = trip.items.filter { it.id != tripItemId }
        repository.updateTrip(trip.copy(items = updatedItems))
    }

    fun addCustomItemToTrip(
        tripId: String,
        name: String,
        quantity: Int,
        category: ItemCategory = ItemCategory.OTHER,
    ) {
        recordHistory()
        val trip = trips.value[tripId] ?: return
        val newItem =
            TripItem(
                id = "custom_${Random.nextInt()}",
                name = name,
                quantity = quantity,
                source = ItemSource.CUSTOM,
                category = category,
            )
        repository.updateTrip(trip.copy(items = trip.items + newItem))
    }

    fun createNewTripType(title: String) {
        recordHistory()
        val newList =
            PackingList(
                id = "list_${Random.nextInt()}",
                title = title,
                itemIds = emptyList(),
            )
        repository.addList(newList)
    }

    fun addItemToTripType(
        listId: String,
        name: String,
        baseQuantity: Int,
        isPerDay: Boolean,
        category: ItemCategory = ItemCategory.OTHER,
        quantityPerDays: Int = 1,
    ) {
        recordHistory()
        val newItemId = "item_${Random.nextInt()}"
        val newItem =
            PackingItem(
                id = newItemId,
                name = name,
                baseQuantity = baseQuantity,
                isPerDay = isPerDay,
                quantityPerDays = quantityPerDays.coerceAtLeast(1),
                category = category,
            )
        repository.addItem(newItem)

        val list = lists.value[listId] ?: return
        repository.addList(list.copy(itemIds = list.itemIds + newItemId))
    }

    fun removeItemFromTripType(
        listId: String,
        itemId: String,
    ) {
        recordHistory()
        val list = lists.value[listId] ?: return
        repository.addList(list.copy(itemIds = list.itemIds - itemId))
    }

    fun updateBaseItemCategory(
        itemId: String,
        category: ItemCategory,
    ) {
        recordHistory()
        val item = items.value[itemId] ?: return
        repository.addItem(item.copy(category = category))
    }

    fun addGeneralItem(
        name: String,
        baseQuantity: Int,
        isPerDay: Boolean,
        category: ItemCategory = ItemCategory.OTHER,
        quantityPerDays: Int = 1,
    ) {
        recordHistory()
        val newItemId = "item_${Random.nextInt()}"
        val newItem =
            PackingItem(
                id = newItemId,
                name = name,
                baseQuantity = baseQuantity,
                isPerDay = isPerDay,
                quantityPerDays = quantityPerDays.coerceAtLeast(1),
                category = category,
            )
        repository.addItem(newItem)

        var generalList = lists.value.values.find { it.isGeneral }
        if (generalList == null) {
            // If repository initialization hasn't finished, create a new one.
            // In practice, this should only happen in tests if we don't wait.
            generalList = PackingList("g1", "General Essentials", emptyList(), isGeneral = true)
        }

        repository.addList(generalList.copy(itemIds = generalList.itemIds + newItemId))
    }

    fun updateBaseItemQuantity(
        itemId: String,
        newQuantity: Int,
    ) {
        if (newQuantity < 1) return
        recordHistory()
        val item = items.value[itemId] ?: return
        repository.addItem(item.copy(baseQuantity = newQuantity))
    }

    fun updateBaseItemQuantityPerDays(
        itemId: String,
        newQuantityPerDays: Int,
    ) {
        if (newQuantityPerDays < 1) return
        recordHistory()
        val item = items.value[itemId] ?: return
        repository.addItem(item.copy(quantityPerDays = newQuantityPerDays))
    }

    fun toggleBaseItemPerDay(itemId: String) {
        recordHistory()
        val item = items.value[itemId] ?: return
        repository.addItem(item.copy(isPerDay = !item.isPerDay))
    }

    fun removeGeneralItem(itemId: String) {
        recordHistory()
        val generalList = lists.value.values.find { it.isGeneral } ?: return@removeGeneralItem
        repository.addList(generalList.copy(itemIds = generalList.itemIds - itemId))
    }

    fun getPlannedTrips(): Flow<List<Trip>> =
        trips.map { tripMap ->
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            tripMap.values.filter { it.endDate >= today }.sortedBy { it.startDate }
        }

    fun getPastTrips(): Flow<List<Trip>> =
        trips.map { tripMap ->
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            tripMap.values.filter { it.endDate < today }.sortedByDescending { it.startDate }
        }

    fun getLists() = lists.value.values.toList()

    fun observeGeneralItems(): Flow<List<PackingItem>> =
        combine(lists, items) { listsMap, itemsMap ->
            val generalList = listsMap.values.find { it.isGeneral } ?: return@combine emptyList<PackingItem>()
            generalList.itemIds.mapNotNull { itemsMap[it] }
        }

    fun observeItemsForList(listId: String): Flow<List<PackingItem>> =
        combine(lists, items) { listsMap, itemsMap ->
            val list = listsMap[listId] ?: return@combine emptyList<PackingItem>()
            list.itemIds.mapNotNull { itemsMap[it] }
        }

    /**
     * Observes the sections for a trip, grouped by source and then category.
     *
     * Groups once to avoid repeated filtering of the same list, which is unnecessarily O(sources × categories × items) per emission.
     */
    fun observeTripSections(tripId: String): Flow<List<SourceSection>> =
        trips.map { tripsMap ->
            val trip = tripsMap[tripId] ?: return@map emptyList()

            val sourceOrder = listOf(ItemSource.ESSENTIAL, ItemSource.ACTIVITY, ItemSource.CUSTOM)
            val categoryOrder = ItemCategory.entries

            val itemsBySource = trip.items.groupBy { it.source }

            sourceOrder.mapNotNull { source ->
                val itemsInSource = itemsBySource[source] ?: return@mapNotNull null
                val itemsByCategory = itemsInSource.groupBy { it.category }

                val categories =
                    categoryOrder.mapNotNull { category ->
                        val itemsInCategory = itemsByCategory[category] ?: return@mapNotNull null
                        CategorySection(category, itemsInCategory)
                    }

                if (categories.isEmpty()) return@mapNotNull null
                SourceSection(source, categories)
            }
        }
}
