package com.github.lucaengel.packpilot.ui.screens.details

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule

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
            .assertTextEquals("Quantity: $qty")
    }
}

fun tripDetailsScreenRobot(composeTestRule: ComposeContentTestRule, block: TripDetailsScreenRobot.() -> Unit) {
    TripDetailsScreenRobot(composeTestRule).apply(block)
}
