package com.github.lucaengel.packpilot.ui.screens.home

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule

class HomeScreenRobot(private val composeTestRule: ComposeContentTestRule) {

    private fun hasTestTagContaining(substring: String): SemanticsMatcher {
        return SemanticsMatcher("TestTag contains $substring") {
            it.config.getOrNull(SemanticsProperties.TestTag)?.contains(substring) == true
        }
    }

    fun clickNewTrip() {
        composeTestRule.onNodeWithContentDescription("New Trip").performClick()
    }

    fun openDrawer() {
        composeTestRule.onNodeWithContentDescription("Open navigation drawer").performClick()
    }

    fun clickDrawerPackingLists() {
        composeTestRule.onNodeWithText("Packing Lists").performClick()
    }

    fun clickDrawerEssentials() {
        composeTestRule.onNodeWithText("Essential Items").performClick()
    }

    fun clickDrawerTripTypes() {
        composeTestRule.onNodeWithText("Trip Types").performClick()
    }

    fun clickTrip(title: String) {
        composeTestRule.onNode(hasTestTagContaining("TripCard_$title")).performClick()
    }

    fun assertTripExists(title: String) {
        composeTestRule.onNode(hasTestTagContaining("TripCard_$title")).assertIsDisplayed()
    }

    fun assertNoTripsMessageDisplayed() {
        composeTestRule.onNodeWithText("No trips planned yet. Tap + to start!").assertIsDisplayed()
    }

    fun assertDrawerMenuButtonDisplayed() {
        composeTestRule.onNodeWithContentDescription("Open navigation drawer").assertIsDisplayed()
    }

    fun assertDrawerIsDisplayed() {
        composeTestRule.onNodeWithText("Packing Lists").assertIsDisplayed()
        composeTestRule.onNodeWithText("Essential Items").assertIsDisplayed()
        composeTestRule.onNodeWithText("Trip Types").assertIsDisplayed()
    }
}

fun homeScreenRobot(composeTestRule: ComposeContentTestRule, block: HomeScreenRobot.() -> Unit) {
    HomeScreenRobot(composeTestRule).apply(block)
}