package com.github.lucaengel.packpilot.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.github.lucaengel.packpilot.App
import com.github.lucaengel.packpilot.repository.DataStoreManager
import com.github.lucaengel.packpilot.repository.PackingRepository
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val dataStore = DataStoreProvider.getDataStore(applicationContext)
        val dataStoreManager = DataStoreManager(dataStore)
        val repository = PackingRepository(dataStoreManager)
        val viewModel = PackingViewModel(repository)
        
        setContent { App(viewModel) }
    }
}
