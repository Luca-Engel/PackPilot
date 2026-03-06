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
}

fun createTripScreenRobot(composeTestRule: ComposeContentTestRule, block: CreateTripScreenRobot.() -> Unit) {
    CreateTripScreenRobot(composeTestRule).apply(block)
}
