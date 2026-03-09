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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Rule
import org.junit.Test

class TripCategoryUiTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun addingCustomItemWithCategoryDisplaysCorrectly() = runTest {
        val testScope = TestScope()
        val repository = PackingRepository(FakeDataStoreManager(), testScope)
        val viewModel = PackingViewModel(repository)

        val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        viewModel.createTrip("Trip", "id", today, today)
        val tripId = viewModel.trips.value.keys.first()

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
            enterCustomItemName("Charger")
            selectCategoryInCustomDialog(ItemCategory.ELECTRONICS)
            clickConfirmAddCustomItem()

            assertItemExists("Charger")
            assertCategory("Charger", ItemCategory.ELECTRONICS)
        }
    }

    @Test
    fun changingCategoryInTripDetailsUpdatesDisplay() = runTest {
        val testScope = TestScope()
        val repository = PackingRepository(FakeDataStoreManager(), testScope)
        val viewModel = PackingViewModel(repository)

        val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        viewModel.createTrip("Trip", "id", today, today)
        val tripId = viewModel.trips.value.keys.first()
        viewModel.addCustomItemToTrip(tripId, "Water", 1, ItemCategory.FOOD)

        composeTestRule.setContent {
            TripDetailsScreen(
                viewModel = viewModel,
                tripId = tripId,
                onBack = {},
            )
        }

        tripDetailsScreenRobot(composeTestRule) {
            assertCategory("Water", ItemCategory.FOOD)

            clickEdit()
            changeCategory("Water", ItemCategory.OTHER)
            assertCategory("Water", ItemCategory.OTHER)
        }
    }
}
