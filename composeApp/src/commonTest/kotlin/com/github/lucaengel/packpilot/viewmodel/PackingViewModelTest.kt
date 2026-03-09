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
    fun testUpdateTripDatesRecomputesQuantities() =
        runTest {
            val fakeDataStore = FakeDataStoreManager()
            val repository = PackingRepository(fakeDataStore, backgroundScope)
            val viewModel = PackingViewModel(repository)

            val generalList = waitForInitialization(viewModel)

            viewModel.addGeneralItem("Socks", 1, true)

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
                        .find { it.name == "Socks" }
                assertEquals(2, socks?.quantity)

                // Update trip length to 5 days
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
    fun testUndoAndRedo() = runTest {
        val fakeDataStore = FakeDataStoreManager()
        val repository = PackingRepository(fakeDataStore, backgroundScope)
        val viewModel = PackingViewModel(repository)

        waitForInitialization(viewModel)

        // Initial state: nothing done yet. Undo/Redo should be false.
        assertEquals(false, viewModel.canUndo.value)
        assertEquals(false, viewModel.canRedo.value)

        // Try calling undo/redo on empty stacks (testing early return branches)
        viewModel.undo()
        viewModel.redo()

        // perform action: add general item
        viewModel.addGeneralItem("UndoItem", 1, false)

        // Wait for it
        viewModel.observeGeneralItems().test {
            var items = awaitItem()
            while (items.none { it.name == "UndoItem" }) {
                items = awaitItem()
            }
            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(viewModel.canUndo.value)
        assertFalse(viewModel.canRedo.value)

        // Undo
        viewModel.undo()
        assertTrue(viewModel.canRedo.value)

        viewModel.observeGeneralItems().test {
            var items = awaitItem()
            while (items.any { it.name == "UndoItem" }) {
                items = awaitItem()
            }
            assertTrue(items.none { it.name == "UndoItem" })
            cancelAndIgnoreRemainingEvents()
        }

        // Redo
        viewModel.redo()
        assertTrue(viewModel.canUndo.value)

        viewModel.observeGeneralItems().test {
            var items = awaitItem()
            while (items.none { it.name == "UndoItem" }) {
                items = awaitItem()
            }
            assertTrue(items.any { it.name == "UndoItem" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testDeleteTrip() = runTest {
        val fakeDataStore = FakeDataStoreManager()
        val repository = PackingRepository(fakeDataStore, backgroundScope)
        val viewModel = PackingViewModel(repository)

        val list = waitForInitialization(viewModel)
        viewModel.createTrip("TripToDelete", list.id, LocalDate(2024, 1, 1), LocalDate(2024, 1, 1))

        viewModel.trips.test {
            var trips = awaitItem()
            while (trips.values.none { it.title == "TripToDelete" }) {
                trips = awaitItem()
            }
            val tripId = trips.values.find { it.title == "TripToDelete" }!!.id

            viewModel.deleteTrip(tripId)

            trips = awaitItem()
            while (trips.containsKey(tripId)) {
                trips = awaitItem()
            }
            assertFalse(trips.containsKey(tripId))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testRemoveTripItem() = runTest {
        val fakeDataStore = FakeDataStoreManager()
        val repository = PackingRepository(fakeDataStore, backgroundScope)
        val viewModel = PackingViewModel(repository)

        val list = waitForInitialization(viewModel)
        viewModel.createTrip("TripToRemoveItem", list.id, LocalDate(2024, 1, 1), LocalDate(2024, 1, 1))

        viewModel.trips.test {
            var trips = awaitItem()
            while (trips.values.none { it.title == "TripToRemoveItem" } || trips.values.first().items.isEmpty()) {
                trips = awaitItem()
            }
            val tripId = trips.values.find { it.title == "TripToRemoveItem" }!!.id
            val itemId = trips[tripId]!!.items.first().id

            viewModel.removeTripItem(tripId, itemId)

            trips = awaitItem()
            while (trips[tripId]?.items?.any { it.id == itemId } == true) {
                trips = awaitItem()
            }
            assertFalse(trips[tripId]!!.items.any { it.id == itemId })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testRemoveItemFromTripType() = runTest {
        val fakeDataStore = FakeDataStoreManager()
        val repository = PackingRepository(fakeDataStore, backgroundScope)
        val viewModel = PackingViewModel(repository)

        waitForInitialization(viewModel)
        viewModel.createNewTripType("TypeToRemove")

        viewModel.lists.test {
            var lists = awaitItem()
            while (lists.values.none { it.title == "TypeToRemove" }) {
                lists = awaitItem()
            }
            val listId = lists.values.find { it.title == "TypeToRemove" }!!.id
            
            viewModel.addItemToTripType(listId, "ItemToRemove", 1, false)
            
            // Wait for item
            viewModel.observeItemsForList(listId).test {
                var items = awaitItem()
                while (items.none { it.name == "ItemToRemove" }) {
                    items = awaitItem()
                }
                val itemId = items.find { it.name == "ItemToRemove" }!!.id
                
                viewModel.removeItemFromTripType(listId, itemId)
                
                items = awaitItem()
                while (items.any { it.id == itemId }) {
                    items = awaitItem()
                }
                assertTrue(items.none { it.id == itemId })
                cancelAndIgnoreRemainingEvents()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testToggleBaseItemPerDay() = runTest {
        val fakeDataStore = FakeDataStoreManager()
        val repository = PackingRepository(fakeDataStore, backgroundScope)
        val viewModel = PackingViewModel(repository)

        waitForInitialization(viewModel)
        viewModel.addGeneralItem("PerDayItem", 1, false)

        viewModel.observeGeneralItems().test {
            var items = awaitItem()
            while (items.none { it.name == "PerDayItem" }) {
                items = awaitItem()
            }
            val itemId = items.find { it.name == "PerDayItem" }!!.id
            assertEquals(false, items.find { it.id == itemId }?.isPerDay)

            viewModel.toggleBaseItemPerDay(itemId)

            items = awaitItem()
            while (items.find { it.id == itemId }?.isPerDay != true) {
                items = awaitItem()
            }
            assertTrue(items.find { it.id == itemId }?.isPerDay == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testGetLists() = runTest {
        val fakeDataStore = FakeDataStoreManager()
        val repository = PackingRepository(fakeDataStore, backgroundScope)
        val viewModel = PackingViewModel(repository)

        waitForInitialization(viewModel)
        
        // Mock data initialization adds one general list
        val lists = viewModel.getLists()
        assertTrue(lists.any { it.isGeneral })
    }

    @Test
    fun testObserveTripSectionsGroupsCorrectly() = runTest {
        val fakeDataStore = FakeDataStoreManager()
        val repository = PackingRepository(fakeDataStore, backgroundScope)
        val viewModel = PackingViewModel(repository)

        val list = waitForInitialization(viewModel)
        val tripId = "test_trip"
        val trip = Trip(
            id = tripId,
            title = "Grouping Trip",
            items = listOf(
                TripItem("1", "Essential Clothing", 1, source = ItemSource.ESSENTIAL, category = ItemCategory.CLOTHING),
                TripItem("2", "Essential Toiletries", 1, source = ItemSource.ESSENTIAL, category = ItemCategory.TOILETRIES),
                TripItem("3", "Custom Other", 1, source = ItemSource.CUSTOM, category = ItemCategory.OTHER),
            )
        )
        repository.addTrip(trip)

        viewModel.observeTripSections(tripId).test {
            val sections = awaitItem()
            
            assertEquals(2, sections.size, "Should have 2 source sections (Essential and Custom)")
            
            val essentialSection = sections.find { it.source == ItemSource.ESSENTIAL }
            assertNotNull(essentialSection)
            assertEquals(2, essentialSection.categories.size, "Essential section should have 2 categories")
            assertTrue(essentialSection.categories.any { it.category == ItemCategory.CLOTHING })
            assertTrue(essentialSection.categories.any { it.category == ItemCategory.TOILETRIES })

            val customSection = sections.find { it.source == ItemSource.CUSTOM }
            assertNotNull(customSection)
            assertEquals(1, customSection.categories.size, "Custom section should have 1 category")
            assertEquals(ItemCategory.OTHER, customSection.categories[0].category)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun testObserveTripSectionsFiltersEmptySections() = runTest {
        val fakeDataStore = FakeDataStoreManager()
        val repository = PackingRepository(fakeDataStore, backgroundScope)
        val viewModel = PackingViewModel(repository)

        val list = waitForInitialization(viewModel)
        val tripId = "test_trip"
        val trip = Trip(
            id = tripId,
            title = "Empty Sections Trip",
            items = listOf(
                TripItem("1", "Essential Only", 1, source = ItemSource.ESSENTIAL, category = ItemCategory.CLOTHING),
            )
        )
        repository.addTrip(trip)

        viewModel.observeTripSections(tripId).test {
            val sections = awaitItem()
            
            assertEquals(1, sections.size, "Should only have the Essential source section")
            assertEquals(ItemSource.ESSENTIAL, sections[0].source)
            assertEquals(1, sections[0].categories.size, "Should only have 1 category section")
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}
