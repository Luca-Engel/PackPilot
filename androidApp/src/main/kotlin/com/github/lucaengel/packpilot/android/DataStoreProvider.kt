package com.github.lucaengel.packpilot.android

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.github.lucaengel.packpilot.repository.DATA_STORE_FILE_NAME
import com.github.lucaengel.packpilot.repository.createDataStore

object DataStoreProvider {
    private var dataStore: DataStore<Preferences>? = null

    fun getDataStore(context: Context): DataStore<Preferences> {
        return dataStore ?: synchronized(this) {
            val instance = createDataStore(
                producePath = { context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath }
            )
            dataStore = instance
            instance
        }
    }
}
