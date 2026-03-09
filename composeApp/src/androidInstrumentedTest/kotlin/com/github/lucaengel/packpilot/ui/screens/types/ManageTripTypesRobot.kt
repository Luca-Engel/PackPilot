package com.github.lucaengel.packpilot.ui.screens.types

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.github.lucaengel.packpilot.model.ItemCategory

class ManageTripTypesRobot(private val composeTestRule: ComposeContentTestRule) {

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
        composeTestRule.waitUntilExactlyOneExists(hasTestTag("TripType_$name"), timeoutMillis = 5000)
        composeTestRule.onNodeWithTag("TripType_$name").performClick()
        // Wait for the sidebar animation and list loading to settle
        composeTestRule.waitForIdle()
    }

    fun clickAddItemToType() {
        composeTestRule.onNodeWithTag("AddItemToType").performClick()
    }

    fun enterItemName(name: String) {
        composeTestRule.onNodeWithTag("ItemNameInput").performTextInput(name)
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
        composeTestRule.waitUntilExactlyOneExists(hasTestTag("BaseItemRow_$itemName"), timeoutMillis = 5000)
        composeTestRule.onNodeWithTag("BaseItemRow_$itemName").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    fun assertCategory(itemName: String, category: ItemCategory) {
        // Wait for the item row to exist first
        composeTestRule.waitUntilExactlyOneExists(hasTestTag("BaseItemRow_$itemName"), timeoutMillis = 5000)
        
        // Match the text anywhere within the row of this specific item. 
        // This is extremely robust against layout changes and semantics merging.
        val matcher = hasText(category.displayName) and hasAnyAncestor(hasTestTag("BaseItemRow_$itemName"))
        
        composeTestRule.waitUntilExactlyOneExists(matcher, timeoutMillis = 5000)
        composeTestRule.onNode(matcher).assertExists()
    }

    fun changeCategory(itemName: String, newCategory: ItemCategory) {
        composeTestRule.onNodeWithTag("BaseCategorySelector_$itemName", useUnmergedTree = true).performClick()
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
