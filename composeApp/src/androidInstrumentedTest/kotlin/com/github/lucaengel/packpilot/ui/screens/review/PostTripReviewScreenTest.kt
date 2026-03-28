package com.github.lucaengel.packpilot.ui.screens.review

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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PostTripReviewScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun buildViewModel(): PackingViewModel {
        val testScope = TestScope()
        val repository = PackingRepository(FakeDataStoreManager(), testScope)
        return PackingViewModel(repository)
    }

    private fun buildViewModelWithTrip(): Pair<PackingViewModel, String> {
        val viewModel = buildViewModel()
        val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        viewModel.addGeneralItem("Umbrella", 1, false, ItemCategory.OTHER)
        viewModel.addGeneralItem("T-Shirt", 3, false, ItemCategory.CLOTHING)
        viewModel.createTrip("London", "city", today, today)
        val tripId = viewModel.trips.value.keys.first()
        return viewModel to tripId
    }

    @Test
    fun tripSummaryCardIsDisplayedWithTripTitle() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            assertTripSummaryCardDisplayed()
            assertTripTitleInSummary("London")
        }
    }

    @Test
    fun progressIndicatorAndTextAreDisplayed() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            assertProgressIndicatorDisplayed()
            assertProgressTextDisplayed()
        }
    }

    @Test
    fun progressStartsAtZeroReviewed() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()
        val totalItems = viewModel.trips.value[tripId]!!.items.size

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            assertProgressText("0 / $totalItems items reviewed")
        }
    }

    @Test
    fun checkingReviewedCheckboxUpdatesProgress() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()
        val totalItems = viewModel.trips.value[tripId]!!.items.size

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            assertProgressText("0 / $totalItems items reviewed")

            clickReviewedCheckbox("Umbrella")
            assertProgressText("1 / $totalItems items reviewed")

            clickReviewedCheckbox("Umbrella")
            assertProgressText("0 / $totalItems items reviewed")
        }
    }

    @Test
    fun itemsAreDisplayedInList() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            assertItemDisplayed("Umbrella")
            assertItemDisplayed("T-Shirt")
        }
    }

    @Test
    fun increasingQuantityUpdatesDisplay() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            assertItemQuantity("Umbrella", 1)
            clickIncreaseQty("Umbrella")
            assertItemQuantity("Umbrella", 2)
        }
    }

    @Test
    fun decreasingQuantityBelowOneIsIgnored() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            assertItemQuantity("Umbrella", 1)
            clickDecreaseQty("Umbrella")
            assertItemQuantity("Umbrella", 1)
        }
    }

    @Test
    fun categoryHeadersAreDisplayedForItemCategories() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            assertCategoryHeaderDisplayed(ItemCategory.CLOTHING.name)
            assertCategoryHeaderDisplayed(ItemCategory.OTHER.name)
        }
    }

    @Test
    fun saveAsTemplateButtonOpensSaveDialog() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            clickSaveAsTemplate()
            assertSaveDialogDisplayed()
        }
    }

    @Test
    fun confirmSaveDisabledWhenTemplateNameBlank() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            clickSaveAsTemplate()
            assertConfirmSaveEnabled(false)

            enterTemplateName("   ")
            assertConfirmSaveEnabled(false)

            enterTemplateName("My Template")
            assertConfirmSaveEnabled(true)
        }
    }

    @Test
    fun savingTemplateUsesAdjustedQuantities() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            // Increase Umbrella qty from 1 to 3
            clickIncreaseQty("Umbrella")
            clickIncreaseQty("Umbrella")

            clickSaveAsTemplate()
            enterTemplateName("London Template")
            clickConfirmSave()
        }

        val templates = viewModel.templates.value
        assertEquals(1, templates.size)
        val savedTemplate = templates.values.first()
        assertEquals("London Template", savedTemplate.name)
        val umbrellaItem = savedTemplate.items.find { it.name == "Umbrella" }
        assertTrue(umbrellaItem != null, "Template should contain Umbrella")
        assertEquals(3, umbrellaItem.quantity, "Umbrella quantity should reflect the adjusted value of 3")
    }

    @Test
    fun backButtonCallsOnBack() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()
        var backCalled = false

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = { backCalled = true })
        }

        postTripReviewScreenRobot(composeTestRule) {
            clickBack()
        }

        assertTrue(backCalled, "Expected onBack to be called when back button is clicked")
    }
}
