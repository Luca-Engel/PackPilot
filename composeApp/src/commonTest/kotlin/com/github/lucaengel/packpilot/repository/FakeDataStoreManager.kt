package com.github.lucaengel.packpilot.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeDataStoreManager : IDataStoreManager {
    private val _itemsJson = MutableStateFlow<String?>(null)
    private val _listsJson = MutableStateFlow<String?>(null)
    private val _tripsJson = MutableStateFlow<String?>(null)
    private val _templatesJson = MutableStateFlow<String?>(null)

    override val itemsJson: Flow<String?> = _itemsJson
    override val listsJson: Flow<String?> = _listsJson
    override val tripsJson: Flow<String?> = _tripsJson
    override val templatesJson: Flow<String?> = _templatesJson

    override suspend fun saveItems(json: String) {
        _itemsJson.value = json
    }

    override suspend fun saveLists(json: String) {
        _listsJson.value = json
    }

    override suspend fun saveTrips(json: String) {
        _tripsJson.value = json
    }

    override suspend fun saveTemplates(json: String) {
        _templatesJson.value = json
    }
}
