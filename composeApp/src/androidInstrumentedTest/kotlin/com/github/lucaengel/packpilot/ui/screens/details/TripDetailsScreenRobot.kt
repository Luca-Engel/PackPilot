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

    fun clickStopEditing() {
        composeTestRule.onNodeWithTag("StopEditingButton").performClick()
    }

    fun confirmDiscardEdits() {
        composeTestRule.onNodeWithTag("ConfirmDiscardEdits").performClick()
    }

    fun assertDiscardDialogIsDisplayed() {
        composeTestRule.onNodeWithTag("ConfirmDiscardEdits").assertIsDisplayed()
    }

    fun assertStopEditingButtonExists() {
        composeTestRule.onNodeWithTag("StopEditingButton").assertExists()
    }

    fun assertDeleteTripButtonExists() {
        composeTestRule.onNodeWithTag("DeleteTripButton").assertExists()
    }

    fun assertDeleteTripButtonDoesNotExist() {
        composeTestRule.onNodeWithTag("DeleteTripButton").assertDoesNotExist()
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
        composeTestRule.onNodeWithTag("CustomItemNameInput").performTextReplacement(name)
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

    fun assertConfirmAddCustomItemEnabled(enabled: Boolean) {
        if (enabled) {
            composeTestRule.onNodeWithTag("ConfirmAddCustomItem").assertIsEnabled()
        } else {
            composeTestRule.onNodeWithTag("ConfirmAddCustomItem").assertIsNotEnabled()
        }
    }

    fun toggleItemPacked(id: String) {
        composeTestRule.onNode(hasTestTagContaining("PackedCheckbox_$id"), useUnmergedTree = true).performClick()
    }

    fun assertCheckboxVisible(id: String) {
        composeTestRule.onNode(hasTestTagContaining("PackedCheckbox_$id"), useUnmergedTree = true).assertIsDisplayed()
    }

    fun assertCheckboxNotVisible(id: String) {
        composeTestRule.onNode(hasTestTagContaining("PackedCheckbox_$id"), useUnmergedTree = true).assertDoesNotExist()
    }

    fun assertItemExistsById(id: String) {
        composeTestRule.onNode(hasTestTagContaining("TripItemRow_$id")).assertIsDisplayed()
    }

    fun assertItemExists(name: String) {
        assertItemExistsById(name)
    }

    fun assertItemDoesNotExist(name: String) {
        composeTestRule.onNode(hasTestTagContaining("TripItemRow_$name")).assertDoesNotExist()
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
        // Assert that the item row with the given ID also contains the correct category in its test tag
        composeTestRule
            .onNode(
                hasTestTagContaining("TripItemRow_$id") and hasTestTagContaining("_CAT_${category.name}"),
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

    fun clickSaveAsTemplate() {
        composeTestRule.onNodeWithTag("SaveAsTemplateButton").performClick()
    }

    fun assertSaveAsTemplateButtonExists() {
        composeTestRule.onNodeWithTag("SaveAsTemplateButton").assertExists()
    }

    fun assertSaveAsTemplateButtonDoesNotExist() {
        composeTestRule.onNodeWithTag("SaveAsTemplateButton").assertDoesNotExist()
    }

    fun enterTemplateName(name: String) {
        composeTestRule.onNodeWithTag("TemplateNameInput").performTextReplacement(name)
    }

    fun clickConfirmSaveAsTemplate() {
        composeTestRule.onNodeWithTag("ConfirmSaveAsTemplate").performClick()
    }

    fun assertSaveAsTemplateDialogDisplayed() {
        composeTestRule.onNodeWithTag("ConfirmSaveAsTemplate").assertIsDisplayed()
    }

    fun assertConfirmSaveAsTemplateEnabled(enabled: Boolean) {
        if (enabled) {
            composeTestRule.onNodeWithTag("ConfirmSaveAsTemplate").assertIsEnabled()
        } else {
            composeTestRule.onNodeWithTag("ConfirmSaveAsTemplate").assertIsNotEnabled()
        }
    }
}

fun tripDetailsScreenRobot(
    composeTestRule: ComposeContentTestRule,
    block: TripDetailsScreenRobot.() -> Unit,
) {
    TripDetailsScreenRobot(composeTestRule).apply(block)
}
