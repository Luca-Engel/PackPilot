package com.github.lucaengel.packpilot.ui.screens.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule

class HomeScreenRobot(private val composeTestRule: ComposeContentTestRule) {

    fun clickNewTrip() {
        composeTestRule.onNodeWithContentDescription("New Trip").performClick()
    }

    fun clickTripTypes() {
        composeTestRule.onNodeWithContentDescription("Trip Types").performClick()
    }

    fun clickEssentials() {
        composeTestRule.onNodeWithContentDescription("Essentials").performClick()
    }

    fun clickTrip(title: String) {
        composeTestRule.onNodeWithTag("TripCard_$title").performClick()
    }

    fun assertTripExists(title: String) {
        composeTestRule.onNodeWithTag("TripCard_$title").assertIsDisplayed()
    }

    fun assertNoTripsMessageDisplayed() {
        composeTestRule.onNodeWithText("No trips planned yet. Tap + to start!").assertIsDisplayed()
    }
}

fun homeScreenRobot(composeTestRule: ComposeContentTestRule, block: HomeScreenRobot.() -> Unit) {
    HomeScreenRobot(composeTestRule).apply(block)
}
