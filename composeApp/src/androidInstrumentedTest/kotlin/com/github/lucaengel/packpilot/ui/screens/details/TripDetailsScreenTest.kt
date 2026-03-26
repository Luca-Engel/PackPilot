package com.github.lucaengel.packpilot.ui.screens.details

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.lucaengel.packpilot.model.*
import com.github.lucaengel.packpilot.repository.FakeDataStoreManager
import com.github.lucaengel.packpilot.repository.PackingRepository
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.*
import org.junit.Rule
import org.junit.Test

class TripDetailsScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun addingCustomItemToTripDisplaysInList() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            val today =
                Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.UTC)
                    .date
            viewModel.createTrip("London", "city", today, today)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(
                    viewModel = viewModel,
                    tripId = tripId,
                    onBack = {},
                )
            }

            tripDetailsScreenRobot(composeTestRule) {
                clickEdit()
                clickAddCustomItem()
                enterCustomItemName("Umbrella")
                enterCustomItemQty("1")
                clickConfirmAddCustomItem()

                assertItemExists("Umbrella")
            }
        }

    @Test
    fun customItemConfirmButtonDisabledWhenInvalid() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            val today =
                Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.UTC)
                    .date
            viewModel.createTrip("London", "city", today, today)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(
                    viewModel = viewModel,
                    tripId = tripId,
                    onBack = {},
                )
            }

            tripDetailsScreenRobot(composeTestRule) {
                clickEdit()
                clickAddCustomItem()

                // Both empty
                assertConfirmAddCustomItemEnabled(false)

                // Only name
                enterCustomItemName("Umbrella")
                assertConfirmAddCustomItemEnabled(false)

                // Qty 0
                enterCustomItemQty("0")
                assertConfirmAddCustomItemEnabled(false)

                // Only qty
                enterCustomItemName("")
                enterCustomItemQty("1")
                assertConfirmAddCustomItemEnabled(false)

                // Both valid
                enterCustomItemName("Umbrella")
                assertConfirmAddCustomItemEnabled(true)

                // Blank name
                enterCustomItemName("   ")
                enterCustomItemQty("1")
                assertConfirmAddCustomItemEnabled(false)
            }
        }

    @Test
    fun tripItemQuantityFollowsRatioRule() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            val today = LocalDate(2024, 1, 1)

            // Add template item: 5 items per 6 days
            viewModel.addGeneralItem("T-Shirts", 5, true, category = ItemCategory.CLOTHING, quantityPerDays = 6)

            // 5-day trip -> ceil(5 * 5/6) = 5
            viewModel.createTrip(
                "RatioTrip",
                "city",
                today,
                today.plus(4, DateTimeUnit.DAY),
            )
            val tripId =
                viewModel.trips.value.values
                    .find { it.title == "RatioTrip" }!!
                    .id

            composeTestRule.setContent {
                TripDetailsScreen(
                    viewModel = viewModel,
                    tripId = tripId,
                    onBack = {},
                )
            }

            tripDetailsScreenRobot(composeTestRule) {
                assertQuantityByName("T-Shirts", 5)
            }
        }

    @Test
    fun perDayClothingWithoutWashingDisplaysCorrectQuantity() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            val start = LocalDate(2024, 1, 1)
            val end = LocalDate(2024, 1, 5) // 5 days

            viewModel.addGeneralItem("Socks", 1, true, category = ItemCategory.CLOTHING, quantityPerDays = 1)
            viewModel.createTrip("Trip", "city", start, end, null)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
            }

            tripDetailsScreenRobot(composeTestRule) {
                assertQuantityByName("Socks", 5)
            }
        }

    @Test
    fun clothingWithWashingIntervalAddsBufferInUI() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            val start = LocalDate(2024, 1, 1)
            val end = LocalDate(2024, 1, 7) // 7 days

            viewModel.addGeneralItem("T-Shirts", 1, true, category = ItemCategory.CLOTHING, quantityPerDays = 1)
            viewModel.createTrip("WashingTrip", "city", start, end, 3)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
            }

            tripDetailsScreenRobot(composeTestRule) {
                // effectiveDays = 3
                // baseQty = 3
                // +1 buffer
                assertQuantityByName("T-Shirts", 4)
            }
        }

    @Test
    fun nonPerDayClothingWithWashingStayOriginalQuantityInUI() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            val start = LocalDate(2024, 1, 1)
            val end = LocalDate(2024, 1, 10) // 10 days

            viewModel.addGeneralItem("Jacket", 1, false, category = ItemCategory.CLOTHING, quantityPerDays = 1)
            viewModel.createTrip("JacketTrip", "city", start, end, 3)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
            }

            tripDetailsScreenRobot(composeTestRule) {
                // baseQuantity = 1 -> no washing for items you don't have a given interval of using for
                assertQuantityByName("Jacket", 1)
            }
        }

    @Test
    fun nonClothingItemIgnoresWashingLogicInUI() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            val start = LocalDate(2024, 1, 1)
            val end = LocalDate(2024, 1, 10) // 10 days

            viewModel.addGeneralItem("Toothbrush", 1, true, category = ItemCategory.OTHER, quantityPerDays = 1)
            viewModel.createTrip("OtherTrip", "city", start, end, 3)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
            }

            tripDetailsScreenRobot(composeTestRule) {
                // expect 10 since once per day and washing does not affect the count
                assertQuantityByName("Toothbrush", 10)
            }
        }

    @Test
    fun perDayClothingWithMultiDayUsageLessItemsThanDaysInUI() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            val start = LocalDate(2024, 1, 1)
            val end = LocalDate(2024, 1, 10) // 10 days

            // 1 every 2 days
            viewModel.addGeneralItem("Shirt", 1, true, category = ItemCategory.CLOTHING, quantityPerDays = 2)
            viewModel.createTrip("MultiDayTrip", "city", start, end, 3)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
            }

            tripDetailsScreenRobot(composeTestRule) {
                // effectiveDays = 3
                // ceil(1*3/2) = 2
                // +1 buffer
                assertQuantityByName("Shirt", 3)
            }
        }

    @Test
    fun perDayClothingWithMultiDayUsageMoreItemsThanDaysInUI() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            val start = LocalDate(2024, 1, 1)
            val end = LocalDate(2024, 1, 11) // 11 days

            // 6 every 5 days
            viewModel.addGeneralItem("Shirt", 6, true, category = ItemCategory.CLOTHING, quantityPerDays = 5)
            viewModel.createTrip("MultiDayTrip2", "city", start, end, 3)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
            }

            tripDetailsScreenRobot(composeTestRule) {
                // effectiveDays = 3
                // ceil(6*3/5) = 4
                // +1 buffer
                assertQuantityByName("Shirt", 5)
            }
        }

    @Test
    fun updatingWashingIntervalInUIUpdatesQuantities() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            val start = LocalDate(2024, 1, 1)
            val end = LocalDate(2024, 1, 7) // 7 days

            viewModel.addGeneralItem("T-Shirts", 1, true, category = ItemCategory.CLOTHING, quantityPerDays = 1)
            viewModel.createTrip("UpdateWashingTrip", "city", start, end, null)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
            }

            tripDetailsScreenRobot(composeTestRule) {
                assertQuantityByName("T-Shirts", 7) // No washing logic yet

                clickEdit()
                enterMaxDaysBetweenWashes("3")

                // effectiveDays = 3
                // baseQty = 3
                // +1 buffer
                assertQuantityByName("T-Shirts", 4)
            }
        }

    @Test
    fun increasingQuantityInEditModeUpdatesDisplay() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            val today =
                Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.UTC)
                    .date
            viewModel.createTrip("London", "city", today, today)
            val tripId =
                viewModel.trips.value.keys
                    .first()
            viewModel.addCustomItemToTrip(tripId, "Umbrella", 1)

            composeTestRule.setContent {
                TripDetailsScreen(
                    viewModel = viewModel,
                    tripId = tripId,
                    onBack = {},
                )
            }

            tripDetailsScreenRobot(composeTestRule) {
                clickEdit()
                assertQuantityByName("Umbrella", 1)

                clickIncreaseQuantity("Umbrella")

                assertQuantityByName("Umbrella", 2)
            }
        }

    @Test
    fun saveEditButtonClosesEditMode() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            val today =
                Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.UTC)
                    .date
            viewModel.createTrip("London", "city", today, today)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
            }

            tripDetailsScreenRobot(composeTestRule) {
                clickEdit()
                clickSave()
                assertEditModeButtonExists()
            }
        }

    @Test
    fun datePickerIsShownInEditModeAndCanBeClosed() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            val today =
                Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.UTC)
                    .date
            viewModel.createTrip("London", "city", today, today)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
            }

            tripDetailsScreenRobot(composeTestRule) {
                clickEdit() // Must be in edit mode now
                clickDatePicker()
                assertDatePickerIsDisplayed()
                clickUpdateDates()
            }
        }

    @Test
    fun customItemQtyOnlyAcceptsDigits() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            val today =
                Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.UTC)
                    .date
            viewModel.createTrip("London", "city", today, today)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
            }

            tripDetailsScreenRobot(composeTestRule) {
                clickEdit()
                clickAddCustomItem()

                // "123" -> accepted
                enterCustomItemQty("123")
                assertCustomItemQty("123")

                // Tries to replace "123" with "12a3" -> rejected, so "123" remains
                enterCustomItemQty("12a3")
                assertCustomItemQty("123")
            }
        }

    @Test
    fun deleteTripDialogIsShown() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            val today =
                Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.UTC)
                    .date
            viewModel.createTrip("London", "city", today, today)
            val tripId =
                viewModel.trips.value.keys
                    .first()

            composeTestRule.setContent {
                TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
            }

            tripDetailsScreenRobot(composeTestRule) {
                clickDeleteTrip()
                assertDeleteDialogIsDisplayed()
            }
        }

    @Test
    fun sectionsAndCategoriesAreDisplayedCorrectly() =
        runTest {
            val testScope = TestScope()
            val repository = PackingRepository(FakeDataStoreManager(), testScope)
            val viewModel = PackingViewModel(repository)

            val tripId = "test_trip"
            val trip =
                Trip(
                    id = tripId,
                    title = "Grouping Trip",
                    activityTitle = "City",
                    items =
                        listOf(
                            TripItem(
                                "1",
                                "Underwear",
                                1,
                                sources =
                                    listOf(
                                        TripItemSourceInfo(
                                            source = ItemSource.ESSENTIAL,
                                            name = "Underwear",
                                            quantity = 1,
                                            category = ItemCategory.CLOTHING,
                                        ),
                                    ),
                                category = ItemCategory.CLOTHING,
                            ),
                            TripItem(
                                "2",
                                "Toothbrush",
                                1,
                                sources =
                                    listOf(
                                        TripItemSourceInfo(
                                            source = ItemSource.ESSENTIAL,
                                            name = "Toothbrush",
                                            quantity = 1,
                                            category = ItemCategory.TOILETRIES,
                                        ),
                                    ),
                                category = ItemCategory.TOILETRIES,
                            ),
                            TripItem(
                                "3",
                                "Umbrella",
                                1,
                                sources =
                                    listOf(
                                        TripItemSourceInfo(
                                            source = ItemSource.CUSTOM,
                                            name = "Umbrella",
                                            quantity = 1,
                                            category = ItemCategory.OTHER,
                                        ),
                                    ),
                                category = ItemCategory.OTHER,
                            ),
                        ),
                )
            repository.addTrip(trip)

            composeTestRule.setContent {
                TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
            }

            tripDetailsScreenRobot(composeTestRule) {
                assertSourceHeaderExists("Essential Items")
                assertCategoryHeaderExists(ItemSource.ESSENTIAL, ItemCategory.CLOTHING)
                assertItemExists("Underwear")
                assertCategoryHeaderExists(ItemSource.ESSENTIAL, ItemCategory.TOILETRIES)
                assertItemExists("Toothbrush")

                assertSourceHeaderExists("Added for this trip")
                assertCategoryHeaderExists(ItemSource.CUSTOM, ItemCategory.OTHER)
                assertItemExists("Umbrella")
            }
        }
}
