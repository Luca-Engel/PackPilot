package com.github.lucaengel.packpilot.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreManager(private val dataStore: DataStore<Preferences>) {
    private val ITEMS_KEY = stringPreferencesKey("items")
    private val LISTS_KEY = stringPreferencesKey("lists")
    private val TRIPS_KEY = stringPreferencesKey("trips")

    val itemsJson: Flow<String?> = dataStore.data.map { it[ITEMS_KEY] }
    val listsJson: Flow<String?> = dataStore.data.map { it[LISTS_KEY] }
    val tripsJson: Flow<String?> = dataStore.data.map { it[TRIPS_KEY] }

    suspend fun saveItems(json: String) {
        dataStore.edit { it[ITEMS_KEY] = json }
    }

    suspend fun saveLists(json: String) {
        dataStore.edit { it[LISTS_KEY] = json }
    }

    suspend fun saveTrips(json: String) {
        dataStore.edit { it[TRIPS_KEY] = json }
    }
}
