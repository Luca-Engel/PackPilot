package com.github.lucaengel.packpilot.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface IDataStoreManager {
    val itemsJson: Flow<String?>
    val listsJson: Flow<String?>
    val tripsJson: Flow<String?>
    val templatesJson: Flow<String?>
    suspend fun saveItems(json: String)
    suspend fun saveLists(json: String)
    suspend fun saveTrips(json: String)
    suspend fun saveTemplates(json: String)
}

open class DataStoreManager(private val dataStore: DataStore<Preferences>) : IDataStoreManager {
    private val ITEMS_KEY = stringPreferencesKey("items")
    private val LISTS_KEY = stringPreferencesKey("lists")
    private val TRIPS_KEY = stringPreferencesKey("trips")
    private val TEMPLATES_KEY = stringPreferencesKey("templates")

    override val itemsJson: Flow<String?> = dataStore.data.map { it[ITEMS_KEY] }
    override val listsJson: Flow<String?> = dataStore.data.map { it[LISTS_KEY] }
    override val tripsJson: Flow<String?> = dataStore.data.map { it[TRIPS_KEY] }
    override val templatesJson: Flow<String?> = dataStore.data.map { it[TEMPLATES_KEY] }

    override suspend fun saveItems(json: String) {
        dataStore.edit { it[ITEMS_KEY] = json }
    }

    override suspend fun saveLists(json: String) {
        dataStore.edit { it[LISTS_KEY] = json }
    }

    override suspend fun saveTrips(json: String) {
        dataStore.edit { it[TRIPS_KEY] = json }
    }

    override suspend fun saveTemplates(json: String) {
        dataStore.edit { it[TEMPLATES_KEY] = json }
    }
}
