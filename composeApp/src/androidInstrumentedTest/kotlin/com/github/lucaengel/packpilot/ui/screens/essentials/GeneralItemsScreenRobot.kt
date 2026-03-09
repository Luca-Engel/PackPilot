package com.github.lucaengel.packpilot.ui.screens.essentials

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.github.lucaengel.packpilot.model.ItemCategory

class GeneralItemsScreenRobot(private val composeTestRule: ComposeContentTestRule) {

    fun clickAddEssential() {
        composeTestRule.onNodeWithContentDescription("Add Essential").performClick()
    }

    fun enterEssentialName(name: String) {
        composeTestRule.onNodeWithTag("EssentialItemNameInput").performTextInput(name)
    }

    fun enterEssentialQuantity(qty: String) {
        composeTestRule.onNodeWithTag("EssentialItemQtyInput").performTextReplacement(qty)
    }

    fun selectCategoryInDialog(category: ItemCategory) {
        composeTestRule.onNodeWithTag("EssentialCategorySelector").performClick()
        composeTestRule.onNodeWithText(category.name).performClick()
    }

    fun clickConfirmAdd() {
        composeTestRule.onNodeWithTag("ConfirmAddEssential").performClick()
    }

    @OptIn(ExperimentalTestApi::class)
    fun assertEssentialExists(name: String) {
        // Wait until it exists in case of async updates
        composeTestRule.waitUntilExactlyOneExists(hasTestTag("BaseItemRow_$name"), timeoutMillis = 5000)
        composeTestRule.onNodeWithTag("BaseItemRow_$name").assertIsDisplayed()
    }

    fun clickDeleteEssential(name: String) {
        composeTestRule.onNodeWithTag("DeleteBaseItem_$name", useUnmergedTree = true).performClick()
    }

    fun clickIncreaseQuantity(name: String) {
        composeTestRule.onNodeWithTag("IncreaseBaseQty_$name", useUnmergedTree = true).performClick()
    }

    fun assertQuantity(name: String, qty: Int) {
        composeTestRule.onNodeWithTag("BaseQtyText_$name", useUnmergedTree = true).assertTextEquals("$qty")
    }

    fun assertCategory(itemName: String, category: ItemCategory) {
        // Using a more robust selector that works even if nodes are merged
        composeTestRule.onNode(
            hasText(category.name) and hasAnyAncestor(hasTestTag("BaseCategorySelector_$itemName")),
            useUnmergedTree = true
        ).assertIsDisplayed()
    }

    fun changeCategory(itemName: String, newCategory: ItemCategory) {
        composeTestRule.onNodeWithTag("BaseCategorySelector_$itemName", useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithText(newCategory.name).performClick()
    }
}

fun generalItemsScreenRobot(composeTestRule: ComposeContentTestRule, block: GeneralItemsScreenRobot.() -> Unit) {
    GeneralItemsScreenRobot(composeTestRule).apply(block)
}
