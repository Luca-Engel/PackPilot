package com.github.lucaengel.packpilot.viewmodel

import com.github.lucaengel.packpilot.model.*
import com.github.lucaengel.packpilot.repository.PackingRepository
import com.github.lucaengel.packpilot.util.DefaultNameNormalizer
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
    private val normalizer = DefaultNameNormalizer()

    val items = repository.items
    val lists = repository.lists
    val trips = repository.trips
    val templates = repository.templates

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

    private var editStartState: AppState? = null

    fun startEditing() {
        editStartState = AppState(items.value, lists.value, trips.value)
        clearHistory()
    }

    fun discardEdits() {
        editStartState?.let { applyState(it) }
        editStartState = null
        clearHistory()
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
        tripId: String,
        startDate: LocalDate,
        endDate: LocalDate,
        maxDaysBetweenWashes: Int? = null,
    ) {
        recordHistory()
        val tripUid = "trip_${Clock.System.now().toEpochMilliseconds()}_${Random.nextInt(1000)}"
        val activityTitle = lists.value[tripId]?.title ?: ""
        val tempTrip =
            Trip(
                id = tripUid,
                title = title,
                startDate = startDate,
                endDate = endDate,
                activityTitle = activityTitle,
                tripTypeId = tripId,
                maxDaysBetweenWashes = maxDaysBetweenWashes,
            )
        val days = tempTrip.days

        val allTripItems = repository.getItemsForTrip(tripId)
        val generalItems = repository.getGeneralItems()
        val generalItemIds = generalItems.map { it.id }
        val tripTypeItems = allTripItems.filter { it.id !in generalItemIds }

        val sourceInfos = mutableListOf<TripItemSourceInfo>()

        generalItems.forEach { item ->
            val qty = calculateQuantity(item, days, maxDaysBetweenWashes)
            sourceInfos.add(
                TripItemSourceInfo(
                    source = ItemSource.ESSENTIAL,
                    name = item.name,
                    quantity = qty,
                    originalItemId = item.id,
                    category = item.category,
                ),
            )
        }

        tripTypeItems.forEach { item ->
            val qty = calculateQuantity(item, days, maxDaysBetweenWashes)
            sourceInfos.add(
                TripItemSourceInfo(
                    source = ItemSource.ACTIVITY,
                    name = item.name,
                    quantity = qty,
                    originalItemId = item.id,
                    category = item.category,
                ),
            )
        }

        val groupedItems = sourceInfos.groupBy { normalizer.normalize(it.name) to it.category }
        val tripItems =
            groupedItems.map { (key, sources) ->
                val (_, category) = key
                val totalQty = sources.sumOf { it.quantity }
                val winningSource = selectWinningSource(sources)

                TripItem(
                    id = "trip_item_${Random.nextInt()}",
                    name = winningSource.name,
                    quantity = totalQty,
                    sources = sources,
                    category = category,
                )
            }

        repository.addTrip(tempTrip.copy(items = tripItems))
    }

    private fun selectWinningSource(sources: List<TripItemSourceInfo>): TripItemSourceInfo {
        // Hierarchy: CUSTOM (most recent) > ACTIVITY > ESSENTIAL
        return sources
            .sortedWith(
                compareByDescending<TripItemSourceInfo> { it.source == ItemSource.CUSTOM }
                    .thenByDescending { it.addedAt }
                    .thenByDescending { it.source == ItemSource.ACTIVITY }
                    .thenByDescending { it.source == ItemSource.ESSENTIAL },
            ).first()
    }

    private fun refreshTrip(trip: Trip): Trip {
        val itemsMap = items.value
        val updatedItems =
            trip.items.map { tripItem ->
                val updatedSources =
                    tripItem.sources.map { source ->
                        if (source.source == ItemSource.CUSTOM || source.originalItemId == null) {
                            source
                        } else {
                            val baseItem = itemsMap[source.originalItemId]
                            if (baseItem != null) {
                                val newQty = calculateQuantity(baseItem, trip.days, trip.maxDaysBetweenWashes)
                                source.copy(quantity = newQty, category = baseItem.category)
                            } else {
                                source
                            }
                        }
                    }
                val totalQty = updatedSources.sumOf { it.quantity }
                val winningSource = selectWinningSource(updatedSources)
                val updatedCategory =
                    if (winningSource.originalItemId != null) {
                        itemsMap[winningSource.originalItemId]?.category ?: tripItem.category
                    } else {
                        tripItem.category
                    }

                tripItem.copy(
                    name = winningSource.name,
                    quantity = totalQty,
                    sources = updatedSources,
                    category = updatedCategory,
                )
            }
        return trip.copy(items = updatedItems)
    }

    private fun syncAffectedTrips(itemId: String) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        trips.value.values
            .filter { trip ->
                trip.startDate >= today &&
                    trip.items.any { item ->
                        item.sources.any { it.originalItemId == itemId }
                    }
            }.forEach { trip ->
                repository.updateTrip(refreshTrip(trip))
            }
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
            trip.items.map { tripItem ->
                val updatedSources =
                    tripItem.sources.map { source ->
                        if (source.source == ItemSource.CUSTOM || source.originalItemId == null) {
                            source
                        } else {
                            val baseItem = itemsMap[source.originalItemId]
                            if (baseItem != null) {
                                val newQty = calculateQuantity(baseItem, newDays, maxDaysBetweenWashes)
                                source.copy(quantity = newQty, category = baseItem.category)
                            } else {
                                source
                            }
                        }
                    }
                val totalQty = updatedSources.sumOf { it.quantity }
                val winningSource = selectWinningSource(updatedSources)

                tripItem.copy(
                    name = winningSource.name,
                    quantity = totalQty,
                    sources = updatedSources,
                    category = winningSource.category,
                )
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
            trip.items.map { item ->
                if (item.id == tripItemId) {
                    val currentAutoQty = item.sources.filter { it.source != ItemSource.CUSTOM }.sumOf { it.quantity }
                    val customSource = item.sources.find { it.source == ItemSource.CUSTOM && it.originalItemId == null }

                    val newSources =
                        if (customSource != null) {
                            item.sources.map {
                                if (it === customSource) {
                                    it.copy(
                                        quantity = newQuantity - currentAutoQty,
                                        addedAt = Clock.System.now().toEpochMilliseconds(),
                                    )
                                } else {
                                    it
                                }
                            }
                        } else {
                            item.sources +
                                TripItemSourceInfo(
                                    source = ItemSource.CUSTOM,
                                    name = item.name,
                                    quantity = newQuantity - currentAutoQty,
                                    addedAt = Clock.System.now().toEpochMilliseconds(),
                                    category = item.category,
                                )
                        }

                    val finalSources = newSources.filter { it.quantity != 0 || it.source != ItemSource.CUSTOM }
                    val winningSource = selectWinningSource(finalSources)

                    item.copy(
                        name = winningSource.name,
                        quantity = newQuantity,
                        sources = finalSources,
                    )
                } else {
                    item
                }
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
                if (it.id == tripItemId) {
                    val updatedSources = it.sources.map { source -> source.copy(category = category) }
                    it.copy(category = category, sources = updatedSources)
                } else {
                    it
                }
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
        val normalizedName = normalizer.normalize(name)
        val existingItem =
            trip.items.find {
                normalizer.normalize(it.name) == normalizedName &&
                    it.category == category
            }

        val updatedItems =
            if (existingItem != null) {
                trip.items.map { item ->
                    if (item.id == existingItem.id) {
                        val newSource =
                            TripItemSourceInfo(
                                source = ItemSource.CUSTOM,
                                name = name,
                                quantity = quantity,
                                addedAt = Clock.System.now().toEpochMilliseconds(),
                                category = category,
                            )
                        val newSources = item.sources + newSource
                        val winningSource = selectWinningSource(newSources)
                        item.copy(
                            name = winningSource.name,
                            quantity = item.quantity + quantity,
                            sources = newSources,
                            category = if (category != ItemCategory.OTHER) category else item.category,
                        )
                    } else {
                        item
                    }
                }
            } else {
                val newItem =
                    TripItem(
                        id = "custom_${Random.nextInt()}",
                        name = name,
                        quantity = quantity,
                        sources =
                            listOf(
                                TripItemSourceInfo(
                                    source = ItemSource.CUSTOM,
                                    name = name,
                                    quantity = quantity,
                                    addedAt = Clock.System.now().toEpochMilliseconds(),
                                    category = category,
                                ),
                            ),
                        category = category,
                    )
                trip.items + newItem
            }
        repository.updateTrip(trip.copy(items = updatedItems))
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
        syncAffectedTrips(itemId)
    }

    /**
     * Creates a new general (i.e., for each trip) item.
     *
     * @param name The name of the item.
     * @param baseQuantity The base quantity of the item.
     * @param isPerDay Whether the item is per day.
     * @param category The category of the item.
     * @param quantityPerDays The quantity per day.
     */
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
        syncAffectedTrips(itemId)
    }

    fun updateBaseItemQuantityPerDays(
        itemId: String,
        newQuantityPerDays: Int,
    ) {
        if (newQuantityPerDays < 1) return
        recordHistory()
        val item = items.value[itemId] ?: return
        repository.addItem(item.copy(quantityPerDays = newQuantityPerDays))
        syncAffectedTrips(itemId)
    }

    fun toggleBaseItemPerDay(itemId: String) {
        recordHistory()
        val item = items.value[itemId] ?: return
        repository.addItem(item.copy(isPerDay = !item.isPerDay))
        syncAffectedTrips(itemId)
    }

    fun removeGeneralItem(itemId: String) {
        recordHistory()
        val generalList = lists.value.values.find { it.isGeneral } ?: return@removeGeneralItem
        repository.addList(generalList.copy(itemIds = generalList.itemIds - itemId))
    }

    fun markTripReviewed(tripId: String) {
        val trip = trips.value[tripId] ?: return
        repository.updateTrip(trip.copy(isReviewed = true))
    }

    fun getTripsAwaitingReview(): Flow<List<Trip>> =
        trips.map { tripMap ->
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            tripMap.values
                .filter { it.endDate < today && !it.isReviewed }
                .sortedByDescending { it.startDate }
        }

    fun saveReviewedTripAsTemplate(
        tripId: String,
        templateName: String,
        adjustedQuantities: Map<String, Int>,
    ) {
        val trip = trips.value[tripId] ?: return
        val templateItems =
            trip.items.map { tripItem ->
                val qty = adjustedQuantities[tripItem.id] ?: tripItem.quantity
                val distinctSources = tripItem.sources.map { it.source }.distinct()
                val effectiveSource =
                    if (distinctSources.size > 1) {
                        ItemSource.MERGED
                    } else {
                        distinctSources.firstOrNull() ?: ItemSource.CUSTOM
                    }
                TemplateItem(
                    name = tripItem.name,
                    quantity = qty,
                    category = tripItem.category,
                    source = effectiveSource,
                )
            }
        val template =
            TripTemplate(
                id = "template_${Clock.System.now().toEpochMilliseconds()}_${Random.nextInt(1000)}",
                name = templateName,
                createdAt = Clock.System.now().toEpochMilliseconds(),
                items = templateItems,
            )
        repository.addTemplate(template)
        markTripReviewed(tripId)
    }

    fun saveCurrentTripAsTemplate(tripId: String, templateName: String) {
        saveReviewedTripAsTemplate(tripId, templateName, emptyMap())
    }

    fun deleteTemplate(templateId: String) {
        repository.deleteTemplate(templateId)
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

            val sourceOrder = listOf(ItemSource.ESSENTIAL, ItemSource.ACTIVITY, ItemSource.CUSTOM, ItemSource.MERGED)
            val categoryOrder = ItemCategory.entries

            val itemsByEffectiveSource =
                trip.items.groupBy { item ->
                    val distinctSources = item.sources.map { it.source }.distinct()
                    if (distinctSources.size > 1) {
                        ItemSource.MERGED
                    } else {
                        distinctSources.firstOrNull() ?: ItemSource.CUSTOM
                    }
                }

            sourceOrder.mapNotNull { source ->
                val itemsInSource = itemsByEffectiveSource[source] ?: return@mapNotNull null
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
