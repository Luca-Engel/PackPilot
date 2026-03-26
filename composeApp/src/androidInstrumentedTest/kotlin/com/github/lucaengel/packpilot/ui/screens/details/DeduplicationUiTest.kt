package com.github.lucaengel.packpilot.ui.screens.details

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.lucaengel.packpilot.model.ItemCategory
import com.github.lucaengel.packpilot.repository.FakeDataStoreManager
import com.github.lucaengel.packpilot.repository.PackingRepository
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.junit.Rule
import org.junit.Test

class DeduplicationUiTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun differentCategoriesDoNotDeduplicate() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            // 1. Essential item (Clothing)
            viewModel.addGeneralItem("T-Shirt", 1, false, category = ItemCategory.CLOTHING)

            // 2. Trip Type item (Toiletries)
            viewModel.createNewTripType("Business")
            val listId =
                viewModel.lists.value.values
                    .find { it.title == "Business" }!!
                    .id
            viewModel.addItemToTripType(listId, "T-Shirt", 1, false, category = ItemCategory.TOILETRIES)

            val today = LocalDate(2024, 1, 1)
            viewModel.createTrip("Test Trip", listId, today, today)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
            }

            tripDetailsScreenRobot(composeTestRule) {
                // 3. Add Custom item (Other) via UI
                clickEdit()
                clickAddCustomItem()
                enterCustomItemName("T-Shirt")
                enterCustomItemQty("1")
                selectCategoryInCustomDialog(ItemCategory.OTHER)
                clickConfirmAddCustomItem()
                clickSave()

                // Expect 3 items with name "T-Shirt"
                assertItemCountWithName("T-Shirt", 3)
            }
        }

    @Test
    fun essentialAndTripSpecificGrouped() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            viewModel.addGeneralItem("Socks", 2, false, category = ItemCategory.CLOTHING)

            val today = LocalDate(2024, 1, 1)
            viewModel.createTrip("Test Trip", "none", today, today)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
            }

            tripDetailsScreenRobot(composeTestRule) {
                clickEdit()
                clickAddCustomItem()
                enterCustomItemName("socks") // lowercase
                enterCustomItemQty("3")
                selectCategoryInCustomDialog(ItemCategory.CLOTHING)
                clickConfirmAddCustomItem()

                // Should be grouped. Name "socks" should win (Custom > Essential)
                assertItemCountWithName("socks", 1)
                assertItemCountWithName("Socks", 0)
                assertQuantityByName("socks", 5) // 2 (essential) + 3 (custom)
            }
        }

    @Test
    fun essentialAndTripTypeGrouped() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            viewModel.addGeneralItem("Soap", 1, false, category = ItemCategory.TOILETRIES)

            viewModel.createNewTripType("Beach")
            val listId =
                viewModel.lists.value.values
                    .find { it.title == "Beach" }!!
                    .id
            viewModel.addItemToTripType(listId, "soap", 2, false, category = ItemCategory.TOILETRIES)

            val today = LocalDate(2024, 1, 1)
            viewModel.createTrip("Test Trip", listId, today, today)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
            }

            tripDetailsScreenRobot(composeTestRule) {
                // Should be grouped. Name "soap" should win (Activity > Essential)
                assertItemCountWithName("soap", 1)
                assertItemCountWithName("Soap", 0)
                assertQuantityByName("soap", 3) // 1 + 2
            }
        }

    @Test
    fun tripTypeAndTripSpecificGrouped() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            viewModel.createNewTripType("Hiking")
            val listId =
                viewModel.lists.value.values
                    .find { it.title == "Hiking" }!!
                    .id
            viewModel.addItemToTripType(listId, "Water", 1, false, category = ItemCategory.FOOD)

            val today = LocalDate(2024, 1, 1)
            viewModel.createTrip("Test Trip", listId, today, today)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
            }

            tripDetailsScreenRobot(composeTestRule) {
                clickEdit()
                clickAddCustomItem()
                enterCustomItemName("water")
                enterCustomItemQty("2")
                selectCategoryInCustomDialog(ItemCategory.FOOD)
                clickConfirmAddCustomItem()

                // Grouped, name "water" wins (Custom > Activity)
                assertItemCountWithName("water", 1)
                assertQuantityByName("water", 3)
            }
        }

    @Test
    fun allThreeSourcesGrouped() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            viewModel.addGeneralItem("HAT", 1, false, category = ItemCategory.CLOTHING)

            viewModel.createNewTripType("Sunny")
            val listId =
                viewModel.lists.value.values
                    .find { it.title == "Sunny" }!!
                    .id
            viewModel.addItemToTripType(listId, "Hat", 1, false, category = ItemCategory.CLOTHING)

            val today = LocalDate(2024, 1, 1)
            viewModel.createTrip("Test Trip", listId, today, today)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
            }

            tripDetailsScreenRobot(composeTestRule) {
                clickEdit()
                clickAddCustomItem()
                enterCustomItemName("hat")
                enterCustomItemQty("1")
                selectCategoryInCustomDialog(ItemCategory.CLOTHING)
                clickConfirmAddCustomItem()

                // Grouped, name "hat" wins
                assertItemCountWithName("hat", 1)
                assertQuantityByName("hat", 3)
            }
        }

    @Test
    fun updatesReflectedInGroupedItemForFutureTrips() =
        // Updates should be reflected in grouped items for future trips
        // we don't update past trips since the user should see what they packed then, and it shouldn't be affected now
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            // Essential: 2 per day -> 20 for 10 days
            viewModel.addGeneralItem("Supplements", 2, true, category = ItemCategory.FOOD, quantityPerDays = 1)
            val essentialId =
                repository.items.value.values
                    .find { it.name == "Supplements" }!!
                    .id

            // Trip Type: 7 fixed -> 7
            viewModel.createNewTripType("Health")
            val listId =
                viewModel.lists.value.values
                    .find { it.title == "Health" }!!
                    .id
            viewModel.addItemToTripType(listId, "supplements", 7, false, category = ItemCategory.FOOD)
            val tripTypeId =
                repository.items.value.values
                    .find { it.name == "supplements" }!!
                    .id

            // start date needs to be in the future since we do not update past dates

            val start =
                Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                    .plus(1, DAY)
            val end = start.plus(9, DAY) // + 9 since the start date also counts -> 10 day trip
            viewModel.createTrip("Long Trip", listId, start, end)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
            }

            tripDetailsScreenRobot(composeTestRule) {
                // Initial: 20 + 7 = 27
                assertQuantityByName("supplements", 27)

                // Update Essential: 3 per day -> 30
                viewModel.updateBaseItemQuantity(essentialId, 3)

                // Update Trip Type: 10 fixed -> 10
                viewModel.updateBaseItemQuantity(tripTypeId, 10)

                // Expected: 30 + 10 = 40
                assertQuantityByName("supplements", 40)
            }
        }

    @Test
    fun updatesNotReflectedInGroupedItemForPastTrips() =
        // Updates should NOT be reflected in grouped items for past trips
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            // Essential: 2 per day -> 20 for 10 days
            viewModel.addGeneralItem("Supplements", 2, true, category = ItemCategory.FOOD, quantityPerDays = 1)
            val essentialId =
                repository.items.value.values
                    .find { it.name == "Supplements" }!!
                    .id

            // Trip Type: 7 fixed -> 7
            viewModel.createNewTripType("Health")
            val listId =
                viewModel.lists.value.values
                    .find { it.title == "Health" }!!
                    .id
            viewModel.addItemToTripType(listId, "supplements", 7, false, category = ItemCategory.FOOD)
            val tripTypeId =
                repository.items.value.values
                    .find { it.name == "supplements" }!!
                    .id

            // start date needs to be in the past
            val start =
                Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                    .minus(11, DAY)
            val end = start.plus(9, DAY) // 10 day trip, ends yesterday (if today is 11th day)
            viewModel.createTrip("Past Trip", listId, start, end)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
            }

            tripDetailsScreenRobot(composeTestRule) {
                // Initial: 20 + 7 = 27
                assertQuantityByName("supplements", 27)

                // Update Essential: 3 per day -> 30 (but shouldn't change the past trip)
                viewModel.updateBaseItemQuantity(essentialId, 3)

                // Update Trip Type: 10 fixed -> 10 (but shouldn't change the past trip)
                viewModel.updateBaseItemQuantity(tripTypeId, 10)

                // Expected: still 27
                assertQuantityByName("supplements", 27)
            }
        }
}
