package com.github.lucaengel.packpilot.ui.screens.details

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.github.lucaengel.packpilot.model.ItemCategory
import com.github.lucaengel.packpilot.model.ItemSource

class TripDetailsScreenRobot(
    private val composeTestRule: ComposeContentTestRule,
) {
    private fun hasTestTagContaining(substring: String): SemanticsMatcher =
        SemanticsMatcher("TestTag contains $substring") {
            it.config.getOrNull(SemanticsProperties.TestTag)?.contains(substring) == true
        }

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
        composeTestRule.onNodeWithTag("CustomItemQtyInput").assert(hasText(qty))
    }

    fun selectCategoryInCustomDialog(category: ItemCategory) {
        composeTestRule.onNodeWithTag("CustomItemCategorySelector").performClick()
        composeTestRule.onNodeWithTag("CategoryItem_${category.name}").performClick()
    }

    fun clickConfirmAddCustomItem() {
        composeTestRule.onNodeWithTag("ConfirmAddCustomItem").performClick()
    }

    fun toggleItemPacked(id: String) {
        composeTestRule.onNode(hasTestTagContaining("PackedCheckbox_$id"), useUnmergedTree = true).performClick()
    }

    fun assertItemExistsById(id: String) {
        composeTestRule.onNode(hasTestTagContaining("TripItemRow_$id")).assertIsDisplayed()
    }

    fun assertItemExists(name: String) {
        assertItemExistsById(name)
    }

    fun assertItemCountWithName(
        name: String,
        count: Int,
    ) {
        composeTestRule
            .onAllNodes(
                hasText(name) and hasAnyAncestor(hasTestTagContaining("TripItemRow_$name")),
                useUnmergedTree = true,
            ).assertCountEquals(count)
    }

    fun clickIncreaseQuantity(id: String) {
        composeTestRule.onNode(hasTestTagContaining("IncreaseQuantity_$id"), useUnmergedTree = true).performClick()
    }

    fun assertQuantityByName(
        name: String,
        qty: Int,
    ) {
        composeTestRule
            .onNode(hasTestTagContaining("ItemQuantity_$name"), useUnmergedTree = true)
            .assertTextEquals("Qty: $qty")
    }

    fun assertCategory(
        id: String,
        category: ItemCategory,
    ) {
        composeTestRule
            .onNode(
                hasText(category.displayName) and hasAnyAncestor(hasTestTagContaining("TripItemRow_$id")),
                useUnmergedTree = true,
            ).assertIsDisplayed()
    }

    fun changeCategory(
        id: String,
        newCategory: ItemCategory,
    ) {
        composeTestRule.onNode(hasTestTagContaining("CategorySelector_$id"), useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithTag("CategoryItem_${newCategory.name}").performClick()
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

    fun assertCategoryHeaderExists(
        source: ItemSource,
        category: ItemCategory,
    ) {
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
