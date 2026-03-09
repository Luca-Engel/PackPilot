package com.github.lucaengel.packpilot.ui.screens.details

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.lucaengel.packpilot.repository.FakeDataStoreManager
import com.github.lucaengel.packpilot.repository.PackingRepository
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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
                clickConfirmAddCustomItem()

                assertItemExists("Umbrella")
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
                assertQuantity("Umbrella", 1)

                clickIncreaseQuantity("Umbrella")

                assertQuantity("Umbrella", 2)
            }
        }

    @Test
    fun saveEditButtonClosesEditMode() = runTest {
        val testScope = TestScope()
        val repository = PackingRepository(FakeDataStoreManager(), testScope)
        val viewModel = PackingViewModel(repository)

        val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        viewModel.createTrip("London", "city", today, today)
        val tripId = viewModel.trips.value.keys.first()

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
    fun datePickerIsShownAndCanBeClosed() = runTest {
        val testScope = TestScope()
        val repository = PackingRepository(FakeDataStoreManager(), testScope)
        val viewModel = PackingViewModel(repository)

        val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        viewModel.createTrip("London", "city", today, today)
        val tripId = viewModel.trips.value.keys.first()

        composeTestRule.setContent {
            TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        tripDetailsScreenRobot(composeTestRule) {
            clickDatePicker()
            assertDatePickerIsDisplayed()
            clickUpdateDates()
        }
    }

    @Test
    fun customItemQtyOnlyAcceptsDigits() = runTest {
        val testScope = TestScope()
        val repository = PackingRepository(FakeDataStoreManager(), testScope)
        val viewModel = PackingViewModel(repository)

        val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        viewModel.createTrip("London", "city", today, today)
        val tripId = viewModel.trips.value.keys.first()

        composeTestRule.setContent {
            TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        tripDetailsScreenRobot(composeTestRule) {
            clickEdit()
            clickAddCustomItem()
            
            // Replaces "1" with "123" -> accepted
            enterCustomItemQty("123")
            assertCustomItemQty("123")
            
            // Tries to replace "123" with "12a3" -> rejected, so "123" remains
            enterCustomItemQty("12a3")
            assertCustomItemQty("123")
        }
    }

    @Test
    fun deleteTripDialogIsShown() = runTest {
        val testScope = TestScope()
        val repository = PackingRepository(FakeDataStoreManager(), testScope)
        val viewModel = PackingViewModel(repository)

        val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        viewModel.createTrip("London", "city", today, today)
        val tripId = viewModel.trips.value.keys.first()

        composeTestRule.setContent {
            TripDetailsScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        tripDetailsScreenRobot(composeTestRule) {
            clickDeleteTrip()
            assertDeleteDialogIsDisplayed()
        }
    }
}
