package com.github.lucaengel.packpilot.ui.screens.create

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule

class CreateTripScreenRobot(private val composeTestRule: ComposeContentTestRule) {

    fun enterTripName(name: String) {
        composeTestRule.onNodeWithTag("TripNameInput").performTextInput(name)
    }

    fun clickSelectTripDates() {
        composeTestRule.onNodeWithTag("DateSelectorCard").performClick()
    }

    fun selectActivityType(type: String) {
        composeTestRule.onNodeWithTag("ActivityType_$type").performClick()
    }

    fun clickConfirmTrip() {
        composeTestRule.onNodeWithTag("ConfirmTripButton").performClick()
    }

    fun assertConfirmButtonEnabled() {
        composeTestRule.onNodeWithTag("ConfirmTripButton").assertIsEnabled()
    }

    fun assertConfirmButtonDisabled() {
        composeTestRule.onNodeWithTag("ConfirmTripButton").assertIsNotEnabled()
    }

    // Mode selector

    fun assertModeSelectorNotVisible() {
        composeTestRule.onNodeWithTag("UseTemplateButton").assertDoesNotExist()
    }

    fun assertModeSelectorVisible() {
        composeTestRule.onNodeWithTag("UseTemplateButton").assertIsDisplayed()
    }

    fun clickStartFromScratch() {
        composeTestRule.onNodeWithTag("StartFromScratchButton").performClick()
    }

    fun clickUseTemplate() {
        composeTestRule.onNodeWithTag("UseTemplateButton").performClick()
    }

    // Template selection

    fun assertTemplateOptionDisplayed(name: String) {
        composeTestRule.onNodeWithTag("TemplateOption_$name").assertIsDisplayed()
    }

    fun selectTemplate(name: String) {
        composeTestRule.onNodeWithTag("TemplateOption_$name").performClick()
    }

    fun assertTemplateSelected(name: String) {
        composeTestRule.onNodeWithTag("TemplateRadioButton_$name").assertIsSelected()
    }

    fun assertTemplateNotSelected(name: String) {
        composeTestRule.onNodeWithTag("TemplateRadioButton_$name").assertIsNotSelected()
    }

    fun assertTemplateGroupHeaderDisplayed(name: String) {
        composeTestRule.onNodeWithTag("TemplateGroupHeader_$name").assertIsDisplayed()
    }

    // Activity type section

    fun assertActivityTypeSectionVisible() {
        composeTestRule.onNodeWithTag("ActivitySearchInput").assertIsDisplayed()
    }

    fun assertActivityTypeSectionNotVisible() {
        composeTestRule.onNodeWithTag("ActivitySearchInput").assertDoesNotExist()
    }
}

fun createTripScreenRobot(composeTestRule: ComposeContentTestRule, block: CreateTripScreenRobot.() -> Unit) {
    CreateTripScreenRobot(composeTestRule).apply(block)
}
