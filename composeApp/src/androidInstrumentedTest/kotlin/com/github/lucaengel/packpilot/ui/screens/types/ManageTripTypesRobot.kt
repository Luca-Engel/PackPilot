package com.github.lucaengel.packpilot.ui.screens.types

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule

class ManageTripTypesRobot(private val composeTestRule: ComposeContentTestRule) {

    fun clickNewType() {
        composeTestRule.onNodeWithContentDescription("New Type").performClick()
    }

    fun enterTypeName(name: String) {
        composeTestRule.onNodeWithTag("NewTypeNameInput").performTextInput(name)
    }

    fun clickCreateType() {
        composeTestRule.onNodeWithText("Create").performClick()
    }

    fun selectType(name: String) {
        composeTestRule.onNodeWithTag("TripType_$name").performClick()
    }

    fun clickAddItemToType() {
        composeTestRule.onNodeWithTag("AddItemToType").performClick()
    }

    fun enterItemName(name: String) {
        composeTestRule.onNodeWithTag("ItemNameInput").performTextInput(name)
    }

    fun clickAdd() {
        composeTestRule.onNodeWithText("Add").performClick()
    }

    fun assertTypeExists(name: String) {
        composeTestRule.onNodeWithText(name).assertIsDisplayed()
    }

    fun assertItemInType(itemName: String) {
        composeTestRule.onNodeWithText(itemName).assertIsDisplayed()
    }
}

fun manageTripTypesRobot(composeTestRule: ComposeContentTestRule, block: ManageTripTypesRobot.() -> Unit) {
    ManageTripTypesRobot(composeTestRule).apply(block)
}
