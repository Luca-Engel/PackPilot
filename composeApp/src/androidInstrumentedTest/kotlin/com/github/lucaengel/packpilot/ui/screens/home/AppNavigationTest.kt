package com.github.lucaengel.packpilot.ui.screens.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.github.lucaengel.packpilot.App
import com.github.lucaengel.packpilot.repository.FakeDataStoreManager
import com.github.lucaengel.packpilot.repository.PackingRepository
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel
import kotlinx.coroutines.test.TestScope
import org.junit.Rule
import org.junit.Test

class AppNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun buildViewModel(): PackingViewModel {
        val testScope = TestScope()
        val repository = PackingRepository(FakeDataStoreManager(), testScope)
        return PackingViewModel(repository)
    }

    @Test
    fun openingDrawerShowsAllNavigationItems() {
        composeTestRule.setContent { App(buildViewModel()) }

        homeScreenRobot(composeTestRule) {
            openDrawer()
            assertDrawerIsDisplayed()
        }
    }

    @Test
    fun drawerNavigatesToEssentialItemsScreen() {
        composeTestRule.setContent { App(buildViewModel()) }

        homeScreenRobot(composeTestRule) {
            openDrawer()
            clickDrawerEssentials()
        }

        // "Add Essential" button is unique to the Essential Items screen
        composeTestRule.onNodeWithContentDescription("Add Essential").assertIsDisplayed()
    }

    @Test
    fun drawerNavigatesToTripTypesScreen() {
        composeTestRule.setContent { App(buildViewModel()) }

        homeScreenRobot(composeTestRule) {
            openDrawer()
            clickDrawerTripTypes()
        }

        // "New Type" button is unique to the Trip Types screen
        composeTestRule.onNodeWithContentDescription("New Type").assertIsDisplayed()
    }

    @Test
    fun drawerPackingListsNavigatesToHomeScreen() {
        composeTestRule.setContent { App(buildViewModel()) }

        homeScreenRobot(composeTestRule) {
            openDrawer()
            clickDrawerPackingLists()
        }

        // "New Trip" FAB is unique to the home screen
        composeTestRule.onNodeWithContentDescription("New Trip").assertIsDisplayed()
    }
}