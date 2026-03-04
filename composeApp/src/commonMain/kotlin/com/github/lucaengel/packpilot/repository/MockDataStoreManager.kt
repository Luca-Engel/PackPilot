package com.github.lucaengel.packpilot.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockDataStoreManager : DataStoreManager(
    dataStore = object : DataStore<Preferences> {
        override val data: Flow<Preferences> = MutableStateFlow(emptyPreferences())
        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            return emptyPreferences()
        }
    }
)
