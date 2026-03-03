package com.github.lucaengel.packpilot.viewmodel

import com.github.lucaengel.packpilot.model.*
import com.github.lucaengel.packpilot.repository.PackingRepository
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.random.Random

class PackingViewModel(private val repository: PackingRepository) {
    val items = repository.items
    val lists = repository.lists
    val trips = repository.trips

    fun createTrip(title: String, listId: String, startDate: LocalDate, endDate: LocalDate) {
        // Use a unique ID based on timestamp and random bits to prevent overwriting
        val tripId = "trip_${Clock.System.now().toEpochMilliseconds()}_${Random.nextInt(1000)}"
        val activityTitle = lists.value[listId]?.title ?: ""
        val tempTrip = Trip(
            id = tripId,
            title = title,
            startDate = startDate,
            endDate = endDate,
            activityTitle = activityTitle,
            baseListId = listId
        )
        val days = tempTrip.days

        val listItems = repository.getItemsForList(listId)
        val generalItems = repository.getGeneralItems()
        val allBaseItems = (listItems + generalItems).distinctBy { it.id }

        val tripItems = allBaseItems.map { item ->
            val calculatedQuantity = if (item.isPerDay) item.baseQuantity * days else item.baseQuantity
            TripItem(
                id = "${item.id}_${Random.nextInt()}",
                name = item.name,
                quantity = calculatedQuantity,
                originalItemId = item.id
            )
        }

        repository.addTrip(tempTrip.copy(items = tripItems))
    }

    fun updateTripDates(tripId: String, startDate: LocalDate, endDate: LocalDate) {
        val trip = trips.value[tripId] ?: return
        repository.updateTrip(trip.copy(startDate = startDate, endDate = endDate))
    }

    fun deleteTrip(tripId: String) {
        repository.deleteTrip(tripId)
    }

    fun togglePacked(tripId: String, tripItemId: String) {
        val trip = trips.value[tripId] ?: return
        val updatedItems = trip.items.map {
            if (it.id == tripItemId) it.copy(isPacked = !it.isPacked) else it
        }
        repository.updateTrip(trip.copy(items = updatedItems))
    }

    fun updateItemQuantity(tripId: String, tripItemId: String, newQuantity: Int) {
        if (newQuantity < 1) return
        val trip = trips.value[tripId] ?: return
        val updatedItems = trip.items.map {
            if (it.id == tripItemId) it.copy(quantity = newQuantity) else it
        }
        repository.updateTrip(trip.copy(items = updatedItems))
    }

    fun removeTripItem(tripId: String, tripItemId: String) {
        val trip = trips.value[tripId] ?: return
        val updatedItems = trip.items.filter { it.id != tripItemId }
        repository.updateTrip(trip.copy(items = updatedItems))
    }

    fun addCustomItemToTrip(tripId: String, name: String, quantity: Int) {
        val trip = trips.value[tripId] ?: return
        val newItem = TripItem(
            id = "custom_${Random.nextInt()}",
            name = name,
            quantity = quantity
        )
        repository.updateTrip(trip.copy(items = trip.items + newItem))
    }

    // Trip Type Management
    fun createNewTripType(title: String) {
        val newList = PackingList(
            id = "list_${Random.nextInt()}",
            title = title,
            itemIds = emptyList()
        )
        repository.addList(newList)
    }

    // General Items Management
    fun addGeneralItem(name: String, baseQuantity: Int, isPerDay: Boolean) {
        val newItemId = "item_${Random.nextInt()}"
        val newItem = PackingItem(
            id = newItemId,
            name = name,
            baseQuantity = baseQuantity,
            isPerDay = isPerDay
        )
        repository.addItem(newItem)
        
        val generalList = lists.value.values.find { it.isGeneral } ?: return
        repository.addList(generalList.copy(itemIds = generalList.itemIds + newItemId))
    }

    fun updateGeneralItemQuantity(itemId: String, newQuantity: Int) {
        if (newQuantity < 1) return
        val item = items.value[itemId] ?: return
        repository.addItem(item.copy(baseQuantity = newQuantity))
    }

    fun toggleGeneralItemPerDay(itemId: String) {
        val item = items.value[itemId] ?: return
        repository.addItem(item.copy(isPerDay = !item.isPerDay))
    }

    fun removeGeneralItem(itemId: String) {
        val generalList = lists.value.values.find { it.isGeneral } ?: return
        repository.addList(generalList.copy(itemIds = generalList.itemIds - itemId))
    }

    // UI Helpers
    fun getPlannedTrips(): Flow<List<Trip>> = trips.map { tripMap ->
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        tripMap.values.filter { it.endDate >= today }.sortedBy { it.startDate }
    }

    fun getPastTrips(): Flow<List<Trip>> = trips.map { tripMap ->
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        tripMap.values.filter { it.endDate < today }.sortedByDescending { it.startDate }
    }

    fun getLists() = lists.value.values.toList()
    
    fun observeGeneralItems(): Flow<List<PackingItem>> = combine(lists, items) { listsMap, itemsMap ->
        val generalList = listsMap.values.find { it.isGeneral } ?: return@combine emptyList<PackingItem>()
        generalList.itemIds.mapNotNull { itemsMap[it] }
    }
}
