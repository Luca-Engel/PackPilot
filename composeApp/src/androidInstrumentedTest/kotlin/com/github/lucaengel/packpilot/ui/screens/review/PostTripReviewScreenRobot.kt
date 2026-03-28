package com.github.lucaengel.packpilot.ui.screens.review

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement

class PostTripReviewScreenRobot(
    private val composeTestRule: ComposeContentTestRule,
) {
    private fun hasTestTagContaining(substring: String): SemanticsMatcher =
        SemanticsMatcher("TestTag contains $substring") {
            it.config.getOrNull(SemanticsProperties.TestTag)?.contains(substring) == true
        }

    fun assertTripSummaryCardDisplayed() {
        composeTestRule.onNodeWithTag("ReviewTripSummaryCard").assertIsDisplayed()
    }

    fun assertProgressTextDisplayed() {
        composeTestRule.onNodeWithTag("ReviewProgressText").assertIsDisplayed()
    }

    fun assertProgressText(text: String) {
        composeTestRule.onNodeWithTag("ReviewProgressText").assertTextEquals(text)
    }

    fun assertProgressIndicatorDisplayed() {
        composeTestRule.onNodeWithTag("ReviewProgressIndicator").assertIsDisplayed()
    }

    fun assertItemDisplayed(name: String) {
        composeTestRule.onNode(hasTestTagContaining("ReviewItemRow_$name")).assertIsDisplayed()
    }

    fun assertItemQuantity(name: String, qty: Int) {
        composeTestRule.onNode(hasTestTagContaining("ReviewItemQty_$name")).assertTextEquals("Qty: $qty")
    }

    fun clickIncreaseQty(name: String) {
        composeTestRule.onNode(hasTestTagContaining("IncreaseReviewQty_$name")).performClick()
    }

    fun clickDecreaseQty(name: String) {
        composeTestRule.onNode(hasTestTagContaining("DecreaseReviewQty_$name")).performClick()
    }

    fun clickReviewedCheckbox(name: String) {
        composeTestRule.onNode(hasTestTagContaining("ReviewedCheckbox_$name")).performClick()
    }

    fun assertCategoryHeaderDisplayed(categoryName: String) {
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
}

fun postTripReviewScreenRobot(
    composeTestRule: ComposeContentTestRule,
    block: PostTripReviewScreenRobot.() -> Unit,
) {
    PostTripReviewScreenRobot(composeTestRule).apply(block)
}
