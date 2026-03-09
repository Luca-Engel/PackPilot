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

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryPersistenceTest {

    private suspend fun waitForInitialization(viewModel: PackingViewModel): PackingList =
        viewModel.lists
            .filter { it.values.any { list -> list.isGeneral } }
            .first()
            .values
            .find { it.isGeneral }!!

    @Test
    fun testAddGeneralItemWithCategory() = runTest {
        val fakeDataStore = FakeDataStoreManager()
        val repository = PackingRepository(fakeDataStore, backgroundScope)
        val viewModel = PackingViewModel(repository)

        waitForInitialization(viewModel)

        viewModel.addGeneralItem("Shirt", 1, true, ItemCategory.CLOTHING)

        viewModel.observeGeneralItems().test {
            var items = awaitItem()
            while (items.none { it.name == "Shirt" }) {
                items = awaitItem()
            }
            val shirt = items.find { it.name == "Shirt" }!!
            assertEquals(ItemCategory.CLOTHING, shirt.category)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testUpdateBaseItemCategory() = runTest {
        val fakeDataStore = FakeDataStoreManager()
        val repository = PackingRepository(fakeDataStore, backgroundScope)
        val viewModel = PackingViewModel(repository)

        waitForInitialization(viewModel)

        viewModel.addGeneralItem("Phone", 1, false, ItemCategory.OTHER)

        viewModel.observeGeneralItems().test {
            var items = awaitItem()
            while (items.none { it.name == "Phone" }) {
                items = awaitItem()
            }
            val phoneId = items.find { it.name == "Phone" }!!.id

            viewModel.updateBaseItemCategory(phoneId, ItemCategory.ELECTRONICS)

            var updatedItems = awaitItem()
            while (updatedItems.find { it.id == phoneId }?.category != ItemCategory.ELECTRONICS) {
                updatedItems = awaitItem()
            }

            assertEquals(ItemCategory.ELECTRONICS, updatedItems.find { it.id == phoneId }?.category)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testTripItemInheritsCategoryFromBaseItem() = runTest {
        val fakeDataStore = FakeDataStoreManager()
        val repository = PackingRepository(fakeDataStore, backgroundScope)
        val viewModel = PackingViewModel(repository)

        val generalList = waitForInitialization(viewModel)
        // Use a unique name not present in mock data to avoid ambiguity
        val itemName = "Identity Card"
        viewModel.addGeneralItem(itemName, 1, false, ItemCategory.DOCUMENTS)

        val start = LocalDate(2024, 1, 1)
        viewModel.createTrip("Travel", generalList.id, start, start)

        viewModel.trips.test {
            var trips = awaitItem()
            while (trips.isEmpty() || trips.values.first().items.none { it.name == itemName }) {
                trips = awaitItem()
            }

            val item = trips.values.first().items.find { it.name == itemName }
            assertEquals(ItemCategory.DOCUMENTS, item?.category)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testAddCustomItemWithCategoryAndThenUpdateIt() = runTest {
        val fakeDataStore = FakeDataStoreManager()
        val repository = PackingRepository(fakeDataStore, backgroundScope)
        val viewModel = PackingViewModel(repository)

        waitForInitialization(viewModel)
        val tripId = "trip_test"
        repository.addTrip(Trip(tripId, "Test Trip"))

        viewModel.addCustomItemToTrip(tripId, "Camera", 1, ItemCategory.ELECTRONICS)

        viewModel.trips.test {
            var trips = awaitItem()
            while (trips[tripId]?.items?.isEmpty() == true) {
                trips = awaitItem()
            }

            val camera = trips[tripId]?.items?.find { it.name == "Camera" }!!
            assertEquals(ItemCategory.ELECTRONICS, camera.category)

            // Update it
            viewModel.updateTripItemCategory(tripId, camera.id, ItemCategory.EQUIPMENT)

            var updatedTrips = awaitItem()
            while (updatedTrips[tripId]?.items?.find { it.id == camera.id }?.category != ItemCategory.EQUIPMENT) {
                updatedTrips = awaitItem()
            }

            assertEquals(ItemCategory.EQUIPMENT, updatedTrips[tripId]?.items?.find { it.id == camera.id }?.category)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
