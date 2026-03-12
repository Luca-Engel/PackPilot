package com.github.lucaengel.packpilot.ui.screens.details

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.github.lucaengel.packpilot.model.ItemCategory
import com.github.lucaengel.packpilot.model.ItemSource

class TripDetailsScreenRobot(
    private val composeTestRule: ComposeContentTestRule,
) {
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

    fun enterCustomItemQty(qty: String) {
        composeTestRule.onNodeWithTag("CustomItemQtyInput").performTextReplacement(qty)
    }

    fun assertCustomItemQty(qty: String) {
        // hasText is more robust for TextFields with labels/hints
        composeTestRule.onNodeWithTag("CustomItemQtyInput").assert(hasText(qty))
    }

    fun selectCategoryInCustomDialog(category: ItemCategory) {
        composeTestRule.onNodeWithTag("CustomItemCategorySelector").performClick()
        composeTestRule.onNodeWithText(category.displayName).performClick()
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

    fun assertQuantity(
        itemName: String,
        qty: Int,
    ) {
        composeTestRule
            .onNodeWithTag("ItemQuantity_$itemName", useUnmergedTree = true)
            .assertTextEquals("Qty: $qty")
    }

    fun assertCategory(
        itemName: String,
        category: ItemCategory,
    ) {
        // In edit mode, it's a selector, in normal mode it's a label
        composeTestRule
            .onNode(
                hasText(category.displayName) and hasAnyAncestor(hasTestTag("TripItemRow_$itemName")),
                useUnmergedTree = true,
            ).assertIsDisplayed()
    }

    fun changeCategory(
        itemName: String,
        newCategory: ItemCategory,
    ) {
        composeTestRule.onNodeWithTag("CategorySelector_$itemName", useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithText(newCategory.displayName).performClick()
    }

    fun clickDatePicker() {
        composeTestRule.onNodeWithTag("DatePickerButton").performClick()
    }

    fun assertDatePickerIsDisplayed() {
        composeTestRule.onNodeWithTag("UpdateDatesConfirm").assertIsDisplayed()
    }

    fun clickUpdateDates() {
        composeTestRule.onNodeWithTag("UpdateDatesConfirm").performClick()
    }

    fun assertDeleteDialogIsDisplayed() {
        composeTestRule.onNodeWithTag("ConfirmDeleteTrip").assertIsDisplayed()
    }

    fun assertEditModeButtonExists() {
        composeTestRule.onNodeWithTag("EditModeButton").assertExists()
    }

    fun assertSourceHeaderExists(title: String) {
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    fun assertCategoryHeaderExists(source: ItemSource, category: ItemCategory) {
        composeTestRule.onNodeWithTag("CategoryHeader_${source.name}_${category.name}").assertIsDisplayed()
    }

    fun enterMaxDaysBetweenWashes(days: String) {
        composeTestRule.onNodeWithTag("EditMaxDaysInput").performTextReplacement(days)
    }
}

fun tripDetailsScreenRobot(
    composeTestRule: ComposeContentTestRule,
    block: TripDetailsScreenRobot.() -> Unit,
) {
    TripDetailsScreenRobot(composeTestRule).apply(block)
}
