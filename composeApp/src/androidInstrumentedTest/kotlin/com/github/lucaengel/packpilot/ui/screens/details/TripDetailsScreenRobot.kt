package com.github.lucaengel.packpilot.ui.screens.details

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.github.lucaengel.packpilot.model.ItemCategory

class TripDetailsScreenRobot(private val composeTestRule: ComposeContentTestRule) {

    fun clickEdit() {
        composeTestRule.onNodeWithTag("EditModeButton").performClick()
    }

    fun clickSave() {
        composeTestRule.onNodeWithTag("SaveEditButton").performClick()
    }

    fun clickDeleteTrip() {
        composeTestRule.onNodeWithTag("DeleteTripButton").performClick()
    }

    fun confirmDelete() {
        composeTestRule.onNodeWithTag("ConfirmDeleteTrip").performClick()
    }

    fun clickAddCustomItem() {
        composeTestRule.onNodeWithTag("AddCustomItemButton").performClick()
    }

    fun enterCustomItemName(name: String) {
        composeTestRule.onNodeWithTag("CustomItemNameInput").performTextInput(name)
    }

    fun selectCategoryInCustomDialog(category: ItemCategory) {
        composeTestRule.onNodeWithTag("CustomItemCategorySelector").performClick()
        composeTestRule.onNodeWithText(category.name).performClick()
    }

    fun clickConfirmAddCustomItem() {
        composeTestRule.onNodeWithTag("ConfirmAddCustomItem").performClick()
    }

    fun toggleItemPacked(name: String) {
        // Using useUnmergedTree because the parent Row/Surface merges semantics
        composeTestRule.onNodeWithTag("PackedCheckbox_$name", useUnmergedTree = true).performClick()
    }

    fun assertItemExists(name: String) {
        composeTestRule.onNodeWithTag("TripItemRow_$name").assertIsDisplayed()
    }

    fun assertItemNotExists(name: String) {
        composeTestRule.onNodeWithTag("TripItemRow_$name").assertDoesNotExist()
    }

    fun clickIncreaseQuantity(itemName: String) {
        composeTestRule.onNodeWithTag("IncreaseQuantity_$itemName", useUnmergedTree = true).performClick()
    }

    fun assertQuantity(itemName: String, qty: Int) {
        composeTestRule.onNodeWithTag("ItemQuantity_$itemName", useUnmergedTree = true)
            .assertTextEquals("Qty: $qty")
    }

    fun assertCategory(itemName: String, category: ItemCategory) {
        // In edit mode, it's a selector, in normal mode it's a label
        composeTestRule.onNode(hasText(category.name) and hasAnyAncestor(hasTestTag("TripItemRow_$itemName")), useUnmergedTree = true)
            .assertIsDisplayed()
    }

    fun changeCategory(itemName: String, newCategory: ItemCategory) {
        composeTestRule.onNodeWithTag("CategorySelector_$itemName", useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithText(newCategory.name).performClick()
    }
}

fun tripDetailsScreenRobot(composeTestRule: ComposeContentTestRule, block: TripDetailsScreenRobot.() -> Unit) {
    TripDetailsScreenRobot(composeTestRule).apply(block)
}
