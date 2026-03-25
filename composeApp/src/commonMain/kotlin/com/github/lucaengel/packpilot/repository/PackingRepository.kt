package com.github.lucaengel.packpilot.repository

import com.github.lucaengel.packpilot.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PackingRepository(
    private val dataStoreManager: IDataStoreManager,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    private val jsonConfig =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    private val _items = MutableStateFlow<Map<String, PackingItem>>(emptyMap())
    val items = _items.asStateFlow()

    private val _lists = MutableStateFlow<Map<String, PackingList>>(emptyMap())
    val lists = _lists.asStateFlow()

    private val _trips = MutableStateFlow<Map<String, Trip>>(emptyMap())
    val trips = _trips.asStateFlow()

    init {
        scope.launch {
            dataStoreManager.itemsJson.collect { json ->
                if (json != null) {
                    try {
                        _items.value = jsonConfig.decodeFromString(json)
                    } catch (e: Exception) {
                        println("Error decoding items: ${e.message}")
                        _items.value = emptyMap()
                        saveItems()
                    }
                } else if (_items.value.isEmpty()) {
                    _items.value = emptyMap() // mockItems
                    saveItems()
                }
            }
        }

        scope.launch {
            dataStoreManager.listsJson.collect { json ->
                if (json != null) {
                    try {
                        _lists.value = jsonConfig.decodeFromString(json)
                    } catch (e: Exception) {
                        println("Error decoding lists: ${e.message}")
                        _lists.value = emptyMap()
                        saveLists()
                    }
                } else if (_lists.value.isEmpty()) {
                    val mockLists =
                        listOf(
                            PackingList("g1", "General Essentials", emptyList(), isGeneral = true),
                        ).associateBy { it.id }
                    _lists.value = mockLists
                    saveLists()
                }
            }
        }

        scope.launch {
            dataStoreManager.tripsJson.collect { json ->
                if (json != null) {
                    try {
                        _trips.value = jsonConfig.decodeFromString(json)
                    } catch (e: Exception) {
                        println("Error decoding trips: ${e.message}")
                        _trips.value = emptyMap()
                        saveTrips()
                    }
                }
            }
        }
    }

    private fun saveItems() =
        scope.launch {
            dataStoreManager.saveItems(jsonConfig.encodeToString(_items.value))
        }

    private fun saveLists() =
        scope.launch {
            dataStoreManager.saveLists(jsonConfig.encodeToString(_lists.value))
        }

    private fun saveTrips() =
        scope.launch {
            dataStoreManager.saveTrips(jsonConfig.encodeToString(_trips.value))
        }

    fun addItem(item: PackingItem) {
        _items.update { it + (item.id to item) }
        saveItems()
    }

    fun addList(list: PackingList) {
        _lists.update { it + (list.id to list) }
        saveLists()
    }

    fun addTrip(trip: Trip) {
        _trips.update { it + (trip.id to trip) }
        saveTrips()
    }

    fun updateTrip(trip: Trip) {
        _trips.update { it + (trip.id to trip) }
        saveTrips()
    }

    fun deleteTrip(tripId: String) {
        _trips.update { it - tripId }
        saveTrips()
    }

    // New helper for Undo/Redo
    fun restoreState(
        items: Map<String, PackingItem>,
        lists: Map<String, PackingList>,
        trips: Map<String, Trip>,
    ) {
        _items.value = items
        _lists.value = lists
        _trips.value = trips
        saveItems()
        saveLists()
        saveTrips()
    }

    fun getGeneralItems(): List<PackingItem> {
        val generalListIds =
            _lists.value.values
                .filter { it.isGeneral }
                .flatMap { it.itemIds }
        return generalListIds.mapNotNull { _items.value[it] }
    }

    fun getItemsForTrip(listId: String): List<PackingItem> {
        val list = _lists.value[listId] ?: return emptyList()
        return list.itemIds.mapNotNull { _items.value[it] }
    }
}
