package com.github.lucaengel.packpilot.viewmodel

import app.cash.turbine.test
import com.github.lucaengel.packpilot.model.*
import com.github.lucaengel.packpilot.repository.FakeDataStoreManager
import com.github.lucaengel.packpilot.repository.PackingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PackingViewModelTest {

    private suspend fun waitForInitialization(viewModel: PackingViewModel): PackingList {
        return viewModel.lists
            .filter { it.values.any { list -> list.isGeneral } }
            .first()
            .values.find { it.isGeneral }!!
    }

    @Test
    fun testCreateTripCalculatesQuantitiesCorrectly() = runTest {
        val fakeDataStore = FakeDataStoreManager()
        val repository = PackingRepository(fakeDataStore, backgroundScope) 
        val viewModel = PackingViewModel(repository)

        val generalList = waitForInitialization(viewModel)

        // Setup test items
        viewModel.addGeneralItem("Underwear", 1, true) // Per day
        viewModel.addGeneralItem("Toothbrush", 1, false) // Total

        val startDate = LocalDate(2024, 1, 1)
        val endDate = LocalDate(2024, 1, 10) // 10 days

        // Create Trip
        viewModel.createTrip("Sicily", generalList.id, startDate, endDate)
        
        viewModel.trips.test {
            var trips = awaitItem()
            // Wait for trip to appear (handle async repo update)
            while (trips.values.none { it.title == "Sicily" }) {
                trips = awaitItem()
            }
            
            val trip = trips.values.find { it.title == "Sicily" }!!
            
            val underwear = trip.items.find { it.name == "Underwear" }
            val toothbrush = trip.items.find { it.name == "Toothbrush" }
            
            assertEquals(10, underwear?.quantity, "Underwear should be 10 (1 per day * 10 days)")
            assertEquals(1, toothbrush?.quantity, "Toothbrush should be 1 (total)")
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testUpdateTripDatesRecomputesQuantities() = runTest {
        val fakeDataStore = FakeDataStoreManager()
        val repository = PackingRepository(fakeDataStore, backgroundScope)
        val viewModel = PackingViewModel(repository)

        val generalList = waitForInitialization(viewModel)

        viewModel.addGeneralItem("Socks", 1, true)
        
        val start = LocalDate(2024, 1, 1)
        viewModel.createTrip("Test", generalList.id, start, start.plus(1, DateTimeUnit.DAY)) // 2 days

        viewModel.trips.test {
            var trips = awaitItem()
            while (trips.isEmpty() || trips.values.first().items.isEmpty()) {
                trips = awaitItem()
            }
            
            val tripId = trips.keys.first()
            val socks = trips.values.first().items.find { it.name == "Socks" }
            assertEquals(2, socks?.quantity)

            // Update to 5 days
            viewModel.updateTripDates(tripId, start, start.plus(4, DateTimeUnit.DAY))
            
            // Wait for update
            var updatedTrips = awaitItem()
            var updatedSocks = updatedTrips[tripId]?.items?.find { it.name == "Socks" }
            while (updatedSocks?.quantity != 5) {
                updatedTrips = awaitItem()
                updatedSocks = updatedTrips[tripId]?.items?.find { it.name == "Socks" }
            }
            
            assertEquals(5, updatedSocks?.quantity, "Quantity should recompute to 5")
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testTogglePacked() = runTest {
        val fakeDataStore = FakeDataStoreManager()
        val repository = PackingRepository(fakeDataStore, backgroundScope)
        val viewModel = PackingViewModel(repository)

        val generalList = waitForInitialization(viewModel)

        viewModel.createTrip("Test", generalList.id, LocalDate(2024, 1, 1), LocalDate(2024, 1, 1))

        viewModel.trips.test {
            var trips = awaitItem()
            while (trips.isEmpty() || trips.values.first().items.isEmpty()) {
                trips = awaitItem()
            }
            
            val tripId = trips.keys.first()
            val itemId = trips.values.first().items.first().id
            
            assertEquals(false, trips.values.first().items.first().isPacked)

            viewModel.togglePacked(tripId, itemId)
            
            var updatedTrips = awaitItem()
            while (updatedTrips[tripId]?.items?.first { it.id == itemId }?.isPacked != true) {
                updatedTrips = awaitItem()
            }
            
            assertTrue(updatedTrips[tripId]?.items?.first { it.id == itemId }?.isPacked == true)
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}
