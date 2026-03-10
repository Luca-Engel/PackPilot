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
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class PackingViewModelTest {
    private suspend fun waitForInitialization(viewModel: PackingViewModel): PackingList =
        viewModel.lists
            .filter { it.values.any { list -> list.isGeneral } }
            .first()
            .values
            .find { it.isGeneral }!!

    @Test
    fun testCreateTripCalculatesQuantitiesCorrectly() =
        runTest {
            val fakeDataStore = FakeDataStoreManager()
            val repository = PackingRepository(fakeDataStore, backgroundScope)
            val viewModel = PackingViewModel(repository)

            val generalList = waitForInitialization(viewModel)

            // Setup test items
            viewModel.addGeneralItem("Test Underwear", 1, true) // Per day
            viewModel.addGeneralItem("Test Toothbrush", 1, false) // Total

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

                val underwear = trip.items.find { it.name == "Test Underwear" }
                val toothbrush = trip.items.find { it.name == "Test Toothbrush" }

                assertEquals(10, underwear?.quantity, "Underwear should be 10 (1 per day * 10 days)")
                assertEquals(1, toothbrush?.quantity, "Toothbrush should be 1 (total)")

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun testCreateTripWithRatioRule() =
        runTest {
            val fakeDataStore = FakeDataStoreManager()
            val repository = PackingRepository(fakeDataStore, backgroundScope)
            val viewModel = PackingViewModel(repository)

            val generalList = waitForInitialization(viewModel)

            // Setup test item: 5 T-shirts per 6 days
            viewModel.addGeneralItem("Ratio T-shirts", 5, true, quantityPerDays = 6)

            val startDate = LocalDate(2024, 1, 1)

            // Trip length: 5 days. Result: ceil(5 * 5/6) = ceil(4.16) = 5
            viewModel.createTrip("Trip 5 days", generalList.id, startDate, startDate.plus(4, DateTimeUnit.DAY))

            viewModel.trips.test {
                var trips = awaitItem()
                while (trips.values.none { it.title == "Trip 5 days" }) {
                    trips = awaitItem()
                }
                val trip = trips.values.find { it.title == "Trip 5 days" }!!
                val tshirts = trip.items.find { it.name == "Ratio T-shirts" }
                assertEquals(5, tshirts?.quantity, "5 T-shirts for 5 days with 5/6 rule")

                // Trip length: 3 days. Result: ceil(3 * 5/6) = ceil(2.5) = 3
                viewModel.createTrip("Trip 3 days", generalList.id, startDate, startDate.plus(2, DateTimeUnit.DAY))
                trips = awaitItem()
                while (trips.values.none { it.title == "Trip 3 days" }) {
                    trips = awaitItem()
                }
                val trip3 = trips.values.find { it.title == "Trip 3 days" }!!
                val tshirts3 = trip3.items.find { it.name == "Ratio T-shirts" }
                assertEquals(3, tshirts3?.quantity, "3 T-shirts for 3 days with 5/6 rule")

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun testUpdateTripDatesWithRatioRule() =
        runTest {
            val fakeDataStore = FakeDataStoreManager()
            val repository = PackingRepository(fakeDataStore, backgroundScope)
            val viewModel = PackingViewModel(repository)

            val generalList = waitForInitialization(viewModel)

            // 1 item per 3 days
            viewModel.addGeneralItem("Ratio Jacket", 1, true, quantityPerDays = 3)

            val start = LocalDate(2024, 1, 1)
            viewModel.createTrip("RatioTest", generalList.id, start, start) // 1 day -> ceil(1 * 1/3) = 1

            viewModel.trips.test {
                var trips = awaitItem()
                while (trips.values.none { it.title == "RatioTest" } ||
                    trips.values
                        .first()
                        .items
                        .isEmpty()
                ) {
                    trips = awaitItem()
                }

                val tripId = trips.values.find { it.title == "RatioTest" }!!.id
                val jacket = trips[tripId]?.items?.find { it.name == "Ratio Jacket" }
                assertEquals(1, jacket?.quantity)

                // Update to 7 days -> ceil(7 * 1/3) = 3
                viewModel.updateTripDates(tripId, start, start.plus(6, DateTimeUnit.DAY))

                var updatedTrips = awaitItem()
                var updatedJacket = updatedTrips[tripId]?.items?.find { it.name == "Ratio Jacket" }
                while (updatedJacket?.quantity != 3) {
                    updatedTrips = awaitItem()
                    updatedJacket = updatedTrips[tripId]?.items?.find { it.name == "Ratio Jacket" }
                }
                assertEquals(3, updatedJacket?.quantity)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun testLaundryAvailability() =
        runTest {
            val fakeDataStore = FakeDataStoreManager()
            val repository = PackingRepository(fakeDataStore, backgroundScope)
            val viewModel = PackingViewModel(repository)

            val generalList = waitForInitialization(viewModel)

            // Using unique names to avoid collisions with repository mock data
            viewModel.addGeneralItem("Laundry Socks", 1, true, category = ItemCategory.CLOTHING)
            viewModel.addGeneralItem("Laundry T-shirts", 5, true, quantityPerDays = 6, category = ItemCategory.CLOTHING)
            viewModel.addGeneralItem("Laundry Toothbrush", 1, false, category = ItemCategory.OTHER)

            val startDate = LocalDate(2024, 1, 1)
            val endDate = LocalDate(2024, 1, 14) // 14 days

            // Case 1: Trip 14 days, Max days 9
            viewModel.createTrip("Laundry Trip", generalList.id, startDate, endDate, maxDaysBetweenWashes = 9)

            viewModel.trips.test {
                var trips = awaitItem()
                while (trips.values.none { it.title == "Laundry Trip" } || trips.values
                        .first()
                        .items.size < 3
                ) {
                    trips = awaitItem()
                }
                val trip = trips.values.find { it.title == "Laundry Trip" }!!

                val socks = trip.items.find { it.name == "Laundry Socks" }
                val tshirts = trip.items.find { it.name == "Laundry T-shirts" }
                val toothbrush = trip.items.find { it.name == "Laundry Toothbrush" }

                // Socks: ceil(9 * 1/1) + 1 = 10
                assertEquals(10, socks?.quantity, "Socks should be 10 for 9 days wash interval on 14 day trip")
                // T-shirts: ceil(9 * 5/6) + 1 = 8 + 1 = 9 (ceil(7.5) = 8, 8+1=9)
                assertEquals(9, tshirts?.quantity, "T-shirts should be 9 for 9 days wash interval on 14 day trip")
                // Toothbrush: unaffected
                assertEquals(1, toothbrush?.quantity)

                // Case 2: Trip 5 days, Max days 9 (Trip shorter than laundry interval)
                viewModel.createTrip(
                    "Short Trip",
                    generalList.id,
                    startDate,
                    startDate.plus(4, DateTimeUnit.DAY),
                    maxDaysBetweenWashes = 9,
                )

                trips = awaitItem()
                while (trips.values.none { it.title == "Short Trip" } ||
                    trips.values
                        .find { it.title == "Short Trip" }!!
                        .items.size < 3
                ) {
                    trips = awaitItem()
                }
                val shortTrip = trips.values.find { it.title == "Short Trip" }!!

                val shortSocks = shortTrip.items.find { it.name == "Laundry Socks" }
                // Socks: ceil(5 * 1/1) = 5 (no buffer because tripDays <= maxDays)
                assertEquals(5, shortSocks?.quantity, "Socks should be 5 for 5 day trip with 9 day wash interval")

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun testUpdateTripDatesRecomputesQuantities() =
        runTest {
            val fakeDataStore = FakeDataStoreManager()
            val repository = PackingRepository(fakeDataStore, backgroundScope)
            val viewModel = PackingViewModel(repository)

            val generalList = waitForInitialization(viewModel)

            viewModel.addGeneralItem("Recompute Socks", 1, true)

            val start = LocalDate(2024, 1, 1)
            viewModel.createTrip("Test", generalList.id, start, start.plus(1, DateTimeUnit.DAY)) // 2 days

            viewModel.trips.test {
                var trips = awaitItem()
                while (trips.isEmpty() ||
                    trips.values
                        .first()
                        .items
                        .isEmpty()
                ) {
                    trips = awaitItem()
                }

                val tripId = trips.keys.first()
                val socks =
                    trips.values
                        .first()
                        .items
                        .find { it.name == "Recompute Socks" }
                assertEquals(2, socks?.quantity)

                // Update trip length to 5 days
                viewModel.updateTripDates(tripId, start, start.plus(4, DateTimeUnit.DAY))

                // Wait for update
                var updatedTrips = awaitItem()
                var updatedSocks = updatedTrips[tripId]?.items?.find { it.name == "Recompute Socks" }
                while (updatedSocks?.quantity != 5) {
                    updatedTrips = awaitItem()
                    updatedSocks = updatedTrips[tripId]?.items?.find { it.name == "Recompute Socks" }
                }

                assertEquals(5, updatedSocks?.quantity, "Quantity should recompute to 5")

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun testTogglePacked() =
        runTest {
            val fakeDataStore = FakeDataStoreManager()
            val repository = PackingRepository(fakeDataStore, backgroundScope)
            val viewModel = PackingViewModel(repository)

            val generalList = waitForInitialization(viewModel)

            viewModel.createTrip("Test", generalList.id, LocalDate(2024, 1, 1), LocalDate(2024, 1, 1))

            viewModel.trips.test {
                var trips = awaitItem()
                while (trips.isEmpty() ||
                    trips.values
                        .first()
                        .items
                        .isEmpty()
                ) {
                    trips = awaitItem()
                }

                val tripId = trips.keys.first()
                val itemId =
                    trips.values
                        .first()
                        .items
                        .first()
                        .id

                assertEquals(
                    false,
                    trips.values
                        .first()
                        .items
                        .first()
                        .isPacked,
                )

                viewModel.togglePacked(tripId, itemId)

                var updatedTrips = awaitItem()
                while (updatedTrips[tripId]?.items?.first { it.id == itemId }?.isPacked != true) {
                    updatedTrips = awaitItem()
                }

                assertTrue(updatedTrips[tripId]?.items?.first { it.id == itemId }?.isPacked == true)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun testUndoAndRedo() =
        runTest {
            val fakeDataStore = FakeDataStoreManager()
            val repository = PackingRepository(fakeDataStore, backgroundScope)
            val viewModel = PackingViewModel(repository)

            waitForInitialization(viewModel)

            // Initial state
            viewModel.createTrip("Trip 1", "g1", LocalDate(2024, 1, 1), LocalDate(2024, 1, 1))

            viewModel.trips.test {
                var trips = awaitItem()
                while (trips.values.none { it.title == "Trip 1" }) {
                    trips = awaitItem()
                }

                // Add an item
                viewModel.addCustomItemToTrip(trips.values.first().id, "New Item", 1)
                trips = awaitItem()
                while (trips.values.first().items.none { it.name == "New Item" }) {
                    trips = awaitItem()
                }

                // Undo
                viewModel.undo()
                trips = awaitItem()
                while (trips.values.first().items.any { it.name == "New Item" }) {
                    trips = awaitItem()
                }
                assertTrue(trips.values.first().items.none { it.name == "New Item" })

                // Redo
                viewModel.redo()
                trips = awaitItem()
                while (trips.values.first().items.none { it.name == "New Item" }) {
                    trips = awaitItem()
                }
                assertTrue(trips.values.first().items.any { it.name == "New Item" })

                cancelAndIgnoreRemainingEvents()
            }
        }
}
