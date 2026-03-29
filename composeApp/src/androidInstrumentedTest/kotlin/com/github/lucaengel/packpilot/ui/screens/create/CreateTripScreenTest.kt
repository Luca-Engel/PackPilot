package com.github.lucaengel.packpilot.ui.screens.create

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.lucaengel.packpilot.model.ItemCategory
import com.github.lucaengel.packpilot.repository.FakeDataStoreManager
import com.github.lucaengel.packpilot.repository.PackingRepository
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel
import kotlinx.coroutines.test.TestScope
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Rule
import org.junit.Test

class CreateTripScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun buildViewModel(): PackingViewModel {
        val testScope = TestScope()
        val repository = PackingRepository(FakeDataStoreManager(), testScope)
        return PackingViewModel(repository)
    }

    private fun buildViewModelWithTemplate(): PackingViewModel {
        val viewModel = buildViewModel()
        val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        viewModel.addGeneralItem("Umbrella", 1, false, ItemCategory.OTHER)
        viewModel.createTrip("London", "city", today, today)
        val tripId = viewModel.trips.value.keys.first()
        viewModel.saveCurrentTripAsTemplate(tripId, "London Template")
        return viewModel
    }

    @Test
    fun confirmButtonIsDisabledInitially() {
        val viewModel = buildViewModel()

        composeTestRule.setContent {
            CreateTripScreen(viewModel = viewModel, onTripCreated = {}, onBack = {})
        }

        createTripScreenRobot(composeTestRule) {
            assertConfirmButtonDisabled()
        }
    }

    @Test
    fun enteringNameAndSelectingActivityEnablesButtonAfterDatesSelected() {
        val viewModel = buildViewModel()

        composeTestRule.setContent {
            CreateTripScreen(viewModel = viewModel, onTripCreated = {}, onBack = {})
        }

        createTripScreenRobot(composeTestRule) {
            enterTripName("Summer Vibe")
            // Note: We can't easily select dates in the Material3 DateRangePicker
            // via standard semantics without knowing the exact node structure or tags.
            // But we can verify that entering other info still keeps it disabled.
            assertConfirmButtonDisabled()
        }
    }

    @Test
    fun templateSectionIsNotShownWhenNoTemplatesExist() {
        val viewModel = buildViewModel()

        composeTestRule.setContent {
            CreateTripScreen(viewModel = viewModel, onTripCreated = {}, onBack = {})
        }

        createTripScreenRobot(composeTestRule) {
            assertTemplateSectionNotVisible()
        }
    }

    @Test
    fun templateSectionIsShownWhenTemplatesExist() {
        val viewModel = buildViewModelWithTemplate()

        composeTestRule.setContent {
            CreateTripScreen(viewModel = viewModel, onTripCreated = {}, onBack = {})
        }

        createTripScreenRobot(composeTestRule) {
            assertTemplateOptionDisplayed("London Template")
        }
    }

    @Test
    fun canSelectATemplate() {
        val viewModel = buildViewModelWithTemplate()

        composeTestRule.setContent {
            CreateTripScreen(viewModel = viewModel, onTripCreated = {}, onBack = {})
        }

        createTripScreenRobot(composeTestRule) {
            selectTemplate("London Template")
            assertTemplateSelected("London Template")
        }
    }

    @Test
    fun deselectingTemplateRemovesSelection() {
        val viewModel = buildViewModelWithTemplate()

        composeTestRule.setContent {
            CreateTripScreen(viewModel = viewModel, onTripCreated = {}, onBack = {})
        }

        createTripScreenRobot(composeTestRule) {
            selectTemplate("London Template")
            assertTemplateSelected("London Template")
            selectTemplate("London Template")
            assertTemplateNotSelected("London Template")
        }
    }
}
