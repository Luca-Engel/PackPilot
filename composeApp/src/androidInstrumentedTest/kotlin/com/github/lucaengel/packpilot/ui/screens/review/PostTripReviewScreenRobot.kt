package com.github.lucaengel.packpilot.ui.screens.review

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement

class PostTripReviewScreenRobot(
    private val composeTestRule: ComposeContentTestRule,
) {
    private fun hasTestTagContaining(substring: String): SemanticsMatcher =
        SemanticsMatcher("TestTag contains $substring") {
            it.config.getOrNull(SemanticsProperties.TestTag)?.contains(substring) == true
        }

    private fun scrollListToNode(matcher: SemanticsMatcher) {
        composeTestRule.onNodeWithTag("ReviewItemsList").performScrollToNode(matcher)
    }

    fun assertTripSummaryCardDisplayed() {
        composeTestRule.onNodeWithTag("ReviewTripSummaryCard").assertIsDisplayed()
    }

    fun assertProgressTextDisplayed() {
        scrollListToNode(hasTestTag("ReviewProgressText"))
        composeTestRule.onNodeWithTag("ReviewProgressText").assertIsDisplayed()
    }

    fun assertProgressText(text: String) {
        scrollListToNode(hasTestTag("ReviewProgressText"))
        composeTestRule.onNodeWithTag("ReviewProgressText").assertTextEquals(text)
    }

    fun assertProgressIndicatorDisplayed() {
        scrollListToNode(hasTestTag("ReviewProgressIndicator"))
        composeTestRule.onNodeWithTag("ReviewProgressIndicator").assertIsDisplayed()
    }

    fun assertItemDisplayed(name: String) {
        val matcher = hasTestTagContaining("ReviewItemRow_$name")
        scrollListToNode(matcher)
        composeTestRule.onNode(matcher).assertIsDisplayed()
    }

    fun assertCategoryHeaderDisplayed(categoryName: String) {
        scrollListToNode(hasTestTag("ReviewCategoryHeader_$categoryName"))
        composeTestRule.onNodeWithTag("ReviewCategoryHeader_$categoryName").assertIsDisplayed()
    }

    fun clickSaveAsTemplate() {
        composeTestRule.onNodeWithTag("SaveAsTemplateFromReviewButton").performClick()
    }

    fun assertSaveDialogDisplayed() {
        composeTestRule.onNodeWithTag("ConfirmSaveReviewTemplate").assertIsDisplayed()
    }

    fun enterTemplateName(name: String) {
        composeTestRule.onNodeWithTag("ReviewTemplateNameInput").performTextReplacement(name)
    }

    fun clickConfirmSave() {
        composeTestRule.onNodeWithTag("ConfirmSaveReviewTemplate").performClick()
    }

    fun assertConfirmSaveEnabled(enabled: Boolean) {
        if (enabled) {
            composeTestRule.onNodeWithTag("ConfirmSaveReviewTemplate").assertIsEnabled()
        } else {
            composeTestRule.onNodeWithTag("ConfirmSaveReviewTemplate").assertIsNotEnabled()
        }
    }

    fun clickBack() {
        composeTestRule.onNodeWithTag("ReviewBackButton").performClick()
    }

    fun assertTripTitleInSummary(title: String) {
        composeTestRule.onNodeWithTag("ReviewTripSummaryTitle").assertIsDisplayed()
    }

    fun clickFeedbackButton(feedbackTypeName: String, itemId: String) {
        val tag = "FeedbackButton_${feedbackTypeName}_$itemId"
        scrollListToNode(hasTestTag(tag))
        composeTestRule.onNodeWithTag(tag).performClick()
    }

    fun assertFeedbackButtonDisplayed(feedbackTypeName: String, itemId: String) {
        val tag = "FeedbackButton_${feedbackTypeName}_$itemId"
        scrollListToNode(hasTestTag(tag))
        composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
    }

    fun assertFeedbackQuantityInputDisplayed(itemId: String) {
        composeTestRule.onNodeWithTag("FeedbackQuantityInput_$itemId").assertIsDisplayed()
    }

    fun enterFeedbackQuantity(itemId: String, qty: String) {
        composeTestRule.onNodeWithTag("FeedbackQuantityInput_$itemId").performTextReplacement(qty)
    }

    fun enterFeedbackPerDays(itemId: String, days: String) {
        composeTestRule.onNodeWithTag("FeedbackPerDaysInput_$itemId").performTextReplacement(days)
    }

    fun clickFeedbackQuantityModeTotal(itemId: String) {
        composeTestRule.onNodeWithTag("FeedbackQuantityModeTotal_$itemId").performClick()
    }

    fun clickFeedbackQuantityModePerDay(itemId: String) {
        composeTestRule.onNodeWithTag("FeedbackQuantityModePerDay_$itemId").performClick()
    }

    fun clickConfirmFeedbackQuantity(itemId: String) {
        composeTestRule.onNodeWithTag("ConfirmFeedbackQuantity_$itemId").performClick()
    }

    fun assertConfirmFeedbackQuantityEnabled(itemId: String, enabled: Boolean) {
        if (enabled) {
            composeTestRule.onNodeWithTag("ConfirmFeedbackQuantity_$itemId").assertIsEnabled()
        } else {
            composeTestRule.onNodeWithTag("ConfirmFeedbackQuantity_$itemId").assertIsNotEnabled()
        }
    }

    fun assertOriginalQtyDisplayed(itemId: String, text: String) {
        val tag = "ReviewItemOriginalQty_$itemId"
        scrollListToNode(hasTestTag(tag))
        composeTestRule.onNodeWithTag(tag).assertTextEquals(text)
    }

    fun assertSuggestedQtyDisplayed(itemId: String, text: String) {
        val tag = "ReviewItemSuggestedQty_$itemId"
        scrollListToNode(hasTestTag(tag))
        composeTestRule.onNodeWithTag(tag).assertTextEquals(text)
    }
}

fun postTripReviewScreenRobot(
    composeTestRule: ComposeContentTestRule,
    block: PostTripReviewScreenRobot.() -> Unit,
) {
    PostTripReviewScreenRobot(composeTestRule).apply(block)
}
