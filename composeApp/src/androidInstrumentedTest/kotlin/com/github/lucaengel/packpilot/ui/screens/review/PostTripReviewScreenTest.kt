package com.github.lucaengel.packpilot.ui.screens.review

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.lucaengel.packpilot.model.FeedbackType
import com.github.lucaengel.packpilot.model.ItemCategory
import com.github.lucaengel.packpilot.repository.FakeDataStoreManager
import com.github.lucaengel.packpilot.repository.PackingRepository
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
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
    fun selectingFeedbackUpdatesProgress() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()
        val totalItems = viewModel.trips.value[tripId]!!.items.size
        val itemId = viewModel.trips.value[tripId]!!.items.find { it.name == "Umbrella" }!!.id

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            assertProgressText("0 / $totalItems items reviewed")
            clickFeedbackButton(FeedbackType.BROUGHT_AND_NEEDED.name, itemId)
            assertProgressText("1 / $totalItems items reviewed")
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
    fun originalQuantityIsDisplayedOnItemRow() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()
        val tripItem = viewModel.trips.value[tripId]!!.items.find { it.name == "Umbrella" }!!

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            assertOriginalQtyDisplayed(tripItem.id, "${tripItem.quantity}")
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
    fun savingTemplateUsesTotalFromQuantityWasOffFeedback() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()
        val itemId = viewModel.trips.value[tripId]!!.items.find { it.name == "Umbrella" }!!.id

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            clickFeedbackButton(FeedbackType.QUANTITY_WAS_OFF.name, itemId)
            enterFeedbackQuantity(itemId, "3")
            clickConfirmFeedbackQuantity(itemId)

            clickSaveAsTemplate()
            enterTemplateName("London Template")
            clickConfirmSave()
        }

        val templates = viewModel.templates.value
        assertEquals(1, templates.size)
        val umbrellaItem = templates.values.first().items.find { it.name == "Umbrella" }
        assertTrue(umbrellaItem != null)
        assertEquals(3, umbrellaItem.quantity, "Template quantity should reflect the user's total suggestion of 3")
    }

    @Test
    fun feedbackChipsAreDisplayedForEachItem() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()
        val itemId = viewModel.trips.value[tripId]!!.items.find { it.name == "Umbrella" }!!.id

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            FeedbackType.entries.forEach { feedbackType ->
                assertFeedbackButtonDisplayed(feedbackType.name, itemId)
            }
        }
    }

    @Test
    fun clickingBroughtAndNeededFeedbackPersistsIt() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()
        val itemId = viewModel.trips.value[tripId]!!.items.find { it.name == "Umbrella" }!!.id

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            clickFeedbackButton(FeedbackType.BROUGHT_AND_NEEDED.name, itemId)
        }

        val feedback = viewModel.trips.value[tripId]!!.itemFeedback
        assertEquals(1, feedback.size)
        assertEquals(FeedbackType.BROUGHT_AND_NEEDED, feedback[0].feedbackType)
        assertEquals(itemId, feedback[0].itemId)
    }

    @Test
    fun clickingBroughtButDidntNeedFeedbackPersistsIt() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()
        val itemId = viewModel.trips.value[tripId]!!.items.find { it.name == "Umbrella" }!!.id

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            clickFeedbackButton(FeedbackType.BROUGHT_BUT_DIDNT_NEED.name, itemId)
        }

        val feedback = viewModel.trips.value[tripId]!!.itemFeedback
        assertEquals(1, feedback.size)
        assertEquals(FeedbackType.BROUGHT_BUT_DIDNT_NEED, feedback[0].feedbackType)
    }

    @Test
    fun clickingNeededButDidntBringFeedbackPersistsIt() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()
        val itemId = viewModel.trips.value[tripId]!!.items.find { it.name == "Umbrella" }!!.id

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            clickFeedbackButton(FeedbackType.NEEDED_BUT_DIDNT_BRING.name, itemId)
        }

        val feedback = viewModel.trips.value[tripId]!!.itemFeedback
        assertEquals(1, feedback.size)
        assertEquals(FeedbackType.NEEDED_BUT_DIDNT_BRING, feedback[0].feedbackType)
    }

    @Test
    fun clickingQuantityWasOffOpensSuggestedQuantityDialog() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()
        val itemId = viewModel.trips.value[tripId]!!.items.find { it.name == "Umbrella" }!!.id

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            clickFeedbackButton(FeedbackType.QUANTITY_WAS_OFF.name, itemId)
            assertFeedbackQuantityInputDisplayed(itemId)
        }
    }

    @Test
    fun confirmFeedbackQuantityDisabledWhenInputEmpty() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()
        val itemId = viewModel.trips.value[tripId]!!.items.find { it.name == "Umbrella" }!!.id

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            clickFeedbackButton(FeedbackType.QUANTITY_WAS_OFF.name, itemId)
            assertConfirmFeedbackQuantityEnabled(itemId, false)

            enterFeedbackQuantity(itemId, "2")
            assertConfirmFeedbackQuantityEnabled(itemId, true)
        }
    }

    @Test
    fun confirmingTotalQuantitySavesFeedbackAndShowsStrikethroughAndNewQty() = runTest {
        val (viewModel, tripId) = buildViewModelWithTrip()
        val tripItem = viewModel.trips.value[tripId]!!.items.find { it.name == "Umbrella" }!!

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            clickFeedbackButton(FeedbackType.QUANTITY_WAS_OFF.name, tripItem.id)
            enterFeedbackQuantity(tripItem.id, "4")
            clickConfirmFeedbackQuantity(tripItem.id)

            // Old quantity still displayed (now strikethrough via decoration, text is same)
            assertOriginalQtyDisplayed(tripItem.id, "${tripItem.quantity}")
            // New suggested quantity shown below
            assertSuggestedQtyDisplayed(tripItem.id, "4")
        }

        val feedback = viewModel.trips.value[tripId]!!.itemFeedback
        assertEquals(FeedbackType.QUANTITY_WAS_OFF, feedback[0].feedbackType)
        assertEquals(4, feedback[0].suggestedQuantity)
        assertEquals(false, feedback[0].suggestedIsPerDay)
    }

    @Test
    fun switchingToPerDayModeAndConfirmingSavesFeedbackWithRate() = runTest {
        val viewModel = buildViewModel()
        val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        // 3-day trip so "1 per 1 day = 3" is easy to verify
        val endDate = today.plus(2, DateTimeUnit.DAY)
        viewModel.addGeneralItem("Socks", 1, false, ItemCategory.CLOTHING)
        viewModel.createTrip("Paris", "city", today, endDate) // 3 days
        val tripId = viewModel.trips.value.keys.first()
        val tripItem = viewModel.trips.value[tripId]!!.items.find { it.name == "Socks" }!!

        composeTestRule.setContent {
            PostTripReviewScreen(viewModel = viewModel, tripId = tripId, onBack = {})
        }

        postTripReviewScreenRobot(composeTestRule) {
            clickFeedbackButton(FeedbackType.QUANTITY_WAS_OFF.name, tripItem.id)
            clickFeedbackQuantityModePerDay(tripItem.id)
            enterFeedbackQuantity(tripItem.id, "1")
            enterFeedbackPerDays(tripItem.id, "1")
            clickConfirmFeedbackQuantity(tripItem.id)

            // Suggested: "1 per day = 3" (3 day trip)
            assertSuggestedQtyDisplayed(tripItem.id, "1 per day = 3")
        }

        val feedback = viewModel.trips.value[tripId]!!.itemFeedback
        assertEquals(FeedbackType.QUANTITY_WAS_OFF, feedback[0].feedbackType)
        assertEquals(true, feedback[0].suggestedIsPerDay)
        assertEquals(1, feedback[0].suggestedBaseQuantity)
        assertEquals(1, feedback[0].suggestedQuantityPerDays)
        assertEquals(3, feedback[0].suggestedQuantity)
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
