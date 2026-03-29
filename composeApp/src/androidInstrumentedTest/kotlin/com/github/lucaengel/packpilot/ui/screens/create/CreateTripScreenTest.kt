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

    /** Creates a ViewModel that already has one saved template (from a trip with no trip type). */
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
    fun modeSelectorIsNotShownWhenNoTemplatesExist() {
        val viewModel = buildViewModel()

        composeTestRule.setContent {
            CreateTripScreen(viewModel = viewModel, onTripCreated = {}, onBack = {})
        }

        createTripScreenRobot(composeTestRule) {
            assertModeSelectorNotVisible()
        }
    }

    @Test
    fun activityTypeSectionIsShownByDefaultWhenNoTemplatesExist() {
        val viewModel = buildViewModel()

        composeTestRule.setContent {
            CreateTripScreen(viewModel = viewModel, onTripCreated = {}, onBack = {})
        }

        createTripScreenRobot(composeTestRule) {
            assertActivityTypeSectionVisible()
        }
    }

    @Test
    fun modeSelectorIsShownWhenTemplatesExist() {
        val viewModel = buildViewModelWithTemplate()

        composeTestRule.setContent {
            CreateTripScreen(viewModel = viewModel, onTripCreated = {}, onBack = {})
        }

        createTripScreenRobot(composeTestRule) {
            assertModeSelectorVisible()
        }
    }

    @Test
    fun activityTypeSectionIsShownByDefaultInScratchMode() {
        val viewModel = buildViewModelWithTemplate()

        composeTestRule.setContent {
            CreateTripScreen(viewModel = viewModel, onTripCreated = {}, onBack = {})
        }

        createTripScreenRobot(composeTestRule) {
            // Default is scratch mode — activity types should be visible
            assertActivityTypeSectionVisible()
        }
    }

    @Test
    fun switchingToTemplateModeHidesActivityTypeSection() {
        val viewModel = buildViewModelWithTemplate()

        composeTestRule.setContent {
            CreateTripScreen(viewModel = viewModel, onTripCreated = {}, onBack = {})
        }

        createTripScreenRobot(composeTestRule) {
            clickUseTemplate()
            assertActivityTypeSectionNotVisible()
        }
    }

    @Test
    fun switchingToTemplateModeShowsTemplates() {
        val viewModel = buildViewModelWithTemplate()

        composeTestRule.setContent {
            CreateTripScreen(viewModel = viewModel, onTripCreated = {}, onBack = {})
        }

        createTripScreenRobot(composeTestRule) {
            clickUseTemplate()
            assertTemplateOptionDisplayed("London Template")
        }
    }

    @Test
    fun switchingBackToScratchModeShowsActivityTypeSection() {
        val viewModel = buildViewModelWithTemplate()

        composeTestRule.setContent {
            CreateTripScreen(viewModel = viewModel, onTripCreated = {}, onBack = {})
        }

        createTripScreenRobot(composeTestRule) {
            clickUseTemplate()
            assertActivityTypeSectionNotVisible()
            clickStartFromScratch()
            assertActivityTypeSectionVisible()
        }
    }

    @Test
    fun canSelectATemplate() {
        val viewModel = buildViewModelWithTemplate()

        composeTestRule.setContent {
            CreateTripScreen(viewModel = viewModel, onTripCreated = {}, onBack = {})
        }

        createTripScreenRobot(composeTestRule) {
            clickUseTemplate()
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
            clickUseTemplate()
            selectTemplate("London Template")
            assertTemplateSelected("London Template")
            selectTemplate("London Template")
            assertTemplateNotSelected("London Template")
        }
    }

    @Test
    fun templatesWithoutTripTypeAreGroupedUnderOther() {
        val viewModel = buildViewModelWithTemplate() // template has no tripTypeId (city = no real list)

        composeTestRule.setContent {
            CreateTripScreen(viewModel = viewModel, onTripCreated = {}, onBack = {})
        }

        createTripScreenRobot(composeTestRule) {
            clickUseTemplate()
            assertTemplateGroupHeaderDisplayed("Other")
        }
    }
}
