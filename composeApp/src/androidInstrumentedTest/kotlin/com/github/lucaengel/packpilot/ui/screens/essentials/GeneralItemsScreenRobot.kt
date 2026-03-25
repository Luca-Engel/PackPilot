package com.github.lucaengel.packpilot.ui.screens.essentials

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.github.lucaengel.packpilot.model.ItemCategory

class GeneralItemsScreenRobot(private val composeTestRule: ComposeContentTestRule) {

    private fun hasTestTagContaining(substring: String): SemanticsMatcher {
        return SemanticsMatcher("TestTag contains $substring") {
            it.config.getOrNull(SemanticsProperties.TestTag)?.contains(substring) == true
        }
    }

    fun clickAddEssential() {
        composeTestRule.onNodeWithContentDescription("Add Essential").performClick()
    }

    fun enterEssentialName(name: String) {
        composeTestRule.onNodeWithTag("EssentialItemNameInput").performTextInput(name)
    }

    fun enterEssentialQuantity(qty: String) {
        composeTestRule.onNodeWithTag("EssentialItemQtyInput").performTextReplacement(qty)
    }

    fun enterEssentialQuantityPerDays(qty: String) {
        composeTestRule.onNodeWithTag("EssentialItemQtyPerDaysInput").performTextReplacement(qty)
    }

    fun clickPerDayCheckbox() {
        composeTestRule.onNodeWithTag("EssentialPerDayCheckbox").performClick()
    }

    fun selectCategoryInDialog(category: ItemCategory) {
        composeTestRule.onNodeWithTag("EssentialCategorySelector").performClick()
        composeTestRule.onNodeWithText(category.displayName).performClick()
    }

    fun clickConfirmAdd() {
        composeTestRule.onNodeWithTag("ConfirmAddEssential").performClick()
    }

    @OptIn(ExperimentalTestApi::class)
    fun assertEssentialExists(name: String) {
        // Wait until it exists in case of async updates
        composeTestRule.waitUntilExactlyOneExists(hasTestTagContaining("BaseItemRow_$name"), timeoutMillis = 5000)
        composeTestRule.onNode(hasTestTagContaining("BaseItemRow_$name")).assertIsDisplayed()
    }

    fun clickDeleteEssential(name: String) {
        composeTestRule.onNode(hasTestTagContaining("DeleteBaseItem_$name"), useUnmergedTree = true).performClick()
    }

    fun clickIncreaseQuantity(name: String) {
        composeTestRule.onNode(hasTestTagContaining("IncreaseBaseQty_$name"), useUnmergedTree = true).performClick()
    }

    fun clickIncreaseQuantityPerDays(name: String) {
        composeTestRule.onNode(hasTestTagContaining("IncreaseBaseQtyPerDays_$name"), useUnmergedTree = true).performClick()
    }

    fun assertQuantity(name: String, qty: Int) {
        composeTestRule.onNode(hasTestTagContaining("BaseQtyText_$name"), useUnmergedTree = true).assertTextEquals("$qty")
    }

    fun assertQuantityPerDays(name: String, qty: Int) {
        composeTestRule.onNode(hasTestTagContaining("BaseQtyPerDaysText_$name"), useUnmergedTree = true).assertTextEquals("$qty")
    }

    fun assertCategory(itemName: String, category: ItemCategory) {
        // Using a more robust selector that works even if nodes are merged
        composeTestRule.onNode(
            hasText(category.displayName) and hasAnyAncestor(hasTestTagContaining("BaseCategorySelector_$itemName")),
            useUnmergedTree = true
        ).assertIsDisplayed()
    }

    fun changeCategory(itemName: String, newCategory: ItemCategory) {
        composeTestRule.onNode(hasTestTagContaining("BaseCategorySelector_$itemName"), useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithText(newCategory.displayName).performClick()
    }
}

fun generalItemsScreenRobot(composeTestRule: ComposeContentTestRule, block: GeneralItemsScreenRobot.() -> Unit) {
    GeneralItemsScreenRobot(composeTestRule).apply(block)
}
