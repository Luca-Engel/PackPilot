package com.github.lucaengel.packpilot.ui.screens.types

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.github.lucaengel.packpilot.model.ItemCategory

class ManageTripTypesRobot(private val composeTestRule: ComposeContentTestRule) {

    private fun hasTestTagContaining(substring: String): SemanticsMatcher {
        return SemanticsMatcher("TestTag contains $substring") {
            it.config.getOrNull(SemanticsProperties.TestTag)?.contains(substring) == true
        }
    }

    fun clickNewType() {
        composeTestRule.onNodeWithContentDescription("New Type").performClick()
    }

    fun enterTypeName(name: String) {
        composeTestRule.onNodeWithTag("NewTypeNameInput").performTextInput(name)
    }

    fun clickCreateType() {
        composeTestRule.onNodeWithTag("ConfirmCreateType").performClick()
    }

    @OptIn(ExperimentalTestApi::class)
    fun selectType(name: String) {
        // We haven't changed TripType_ yet, but using containing for future-proofing
        composeTestRule.waitUntilExactlyOneExists(hasTestTagContaining("TripType_$name"), timeoutMillis = 5000)
        composeTestRule.onNode(hasTestTagContaining("TripType_$name")).performClick()
        // Wait for the sidebar animation and list loading to settle
        composeTestRule.waitForIdle()
    }

    fun clickAddItemToType() {
        composeTestRule.onNodeWithTag("AddItemToType").performClick()
    }

    fun enterItemName(name: String) {
        composeTestRule.onNodeWithTag("ItemNameInput").performTextInput(name)
    }

    fun enterItemQuantity(qty: String) {
        composeTestRule.onNodeWithTag("ItemQtyInput").performTextReplacement(qty)
    }

    fun enterItemQuantityPerDays(qty: String) {
        composeTestRule.onNodeWithTag("ItemQtyPerDaysInput").performTextReplacement(qty)
    }

    fun clickPerDayCheckbox() {
        composeTestRule.onNodeWithTag("PerDayCheckbox").performClick()
    }

    fun selectCategoryInAddDialog(category: ItemCategory) {
        composeTestRule.onNodeWithTag("AddItemCategorySelector").performClick()
        composeTestRule.onNodeWithText(category.displayName).performClick()
    }

    fun clickAdd() {
        composeTestRule.onNodeWithTag("ConfirmAddItemToType").performClick()
    }

    fun assertTypeExists(name: String) {
        composeTestRule.onNodeWithText(name).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    fun assertItemInType(itemName: String) {
        composeTestRule.waitUntilExactlyOneExists(hasTestTagContaining("BaseItemRow_$itemName"), timeoutMillis = 5000)
        composeTestRule.onNode(hasTestTagContaining("BaseItemRow_$itemName")).assertIsDisplayed()
    }

    fun assertQuantity(name: String, qty: Int) {
        composeTestRule.onNode(hasTestTagContaining("BaseQtyText_$name"), useUnmergedTree = true).assertTextEquals("$qty")
    }

    fun assertQuantityPerDays(name: String, qty: Int) {
        composeTestRule.onNode(hasTestTagContaining("BaseQtyPerDaysText_$name"), useUnmergedTree = true).assertTextEquals("$qty")
    }

    @OptIn(ExperimentalTestApi::class)
    fun assertCategory(itemName: String, category: ItemCategory) {
        // Wait for the item row to exist first
        composeTestRule.waitUntilExactlyOneExists(hasTestTagContaining("BaseItemRow_$itemName"), timeoutMillis = 5000)
        
        // Match the text anywhere within the row of this specific item. 
        // This is extremely robust against layout changes and semantics merging.
        val matcher = hasText(category.displayName) and hasAnyAncestor(hasTestTagContaining("BaseItemRow_$itemName"))
        
        composeTestRule.waitUntilExactlyOneExists(matcher, timeoutMillis = 5000)
        composeTestRule.onNode(matcher).assertExists()
    }

    fun changeCategory(itemName: String, newCategory: ItemCategory) {
        composeTestRule.onNode(hasTestTagContaining("BaseCategorySelector_$itemName"), useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithText(newCategory.displayName).performClick()
    }

    fun assertSidebarVisible() {
        val matcher = SemanticsMatcher("has TripType tag") { 
            val tag = it.config.getOrElse(SemanticsProperties.TestTag) { "" }
            tag.startsWith("TripType_")
        }
        composeTestRule.onAllNodes(matcher).onFirst().assertIsDisplayed()
    }

    fun assertSidebarHidden() {
        val matcher = SemanticsMatcher("has TripType tag") { 
            val tag = it.config.getOrElse(SemanticsProperties.TestTag) { "" }
            tag.startsWith("TripType_")
        }
        composeTestRule.onAllNodes(matcher).assertCountEquals(0)
    }

    fun toggleSidebar() {
        composeTestRule.onNodeWithContentDescription("Toggle Sidebar").performClick()
    }
}

fun manageTripTypesRobot(composeTestRule: ComposeContentTestRule, block: ManageTripTypesRobot.() -> Unit) {
    ManageTripTypesRobot(composeTestRule).apply(block)
}
