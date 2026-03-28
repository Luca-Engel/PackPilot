package com.github.lucaengel.packpilot.ui.screens.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.lucaengel.packpilot.model.Trip
import com.github.lucaengel.packpilot.repository.FakeDataStoreManager
import com.github.lucaengel.packpilot.repository.PackingRepository
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyStateDisplaysNoTripsMessage() {
        val testScope = TestScope()
        val repository = PackingRepository(FakeDataStoreManager(), testScope)
        val viewModel = PackingViewModel(repository)

        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onCreateTrip = {},
                onSelectTrip = {},
                onOpenDrawer = {},
            )
        }

        homeScreenRobot(composeTestRule) {
            assertNoTripsMessageDisplayed()
        }
    }

    @Test
    fun plannedTripsAreDisplayedInList() = runTest {
        val testScope = TestScope()
        val repository = PackingRepository(FakeDataStoreManager(), testScope)
        val viewModel = PackingViewModel(repository)

        val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
        viewModel.createTrip("Ski Trip", "skiing", today, today)

        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onCreateTrip = {},
                onSelectTrip = {},
                onOpenDrawer = {},
            )
        }

        homeScreenRobot(composeTestRule) {
            assertTripExists("Ski Trip")
        }
    }

    @Test
    fun drawerMenuButtonIsDisplayed() {
        val testScope = TestScope()
        val repository = PackingRepository(FakeDataStoreManager(), testScope)
        val viewModel = PackingViewModel(repository)

        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onCreateTrip = {},
                onSelectTrip = {},
                onOpenDrawer = {},
            )
        }

        homeScreenRobot(composeTestRule) {
            assertDrawerMenuButtonDisplayed()
        }
    }

    @Test
    fun reviewPromptIsShownForUnreviewedPastTrip() = runTest {
        val testScope = TestScope()
        val repository = PackingRepository(FakeDataStoreManager(), testScope)
        val viewModel = PackingViewModel(repository)

        val pastDate = LocalDate(2020, 1, 1)
        repository.addTrip(Trip(id = "t1", title = "Old Adventure", startDate = pastDate, endDate = pastDate, isReviewed = false))

        composeTestRule.setContent {
            HomeScreen(viewModel = viewModel, onCreateTrip = {}, onSelectTrip = {}, onOpenDrawer = {})
        }

        homeScreenRobot(composeTestRule) {
            assertReviewPromptDisplayed("Old Adventure")
        }
    }

    @Test
    fun reviewPromptIsNotShownForAlreadyReviewedTrip() = runTest {
        val testScope = TestScope()
        val repository = PackingRepository(FakeDataStoreManager(), testScope)
        val viewModel = PackingViewModel(repository)

        val pastDate = LocalDate(2020, 1, 1)
        repository.addTrip(Trip(id = "t1", title = "Reviewed Trip", startDate = pastDate, endDate = pastDate, isReviewed = true))

        composeTestRule.setContent {
            HomeScreen(viewModel = viewModel, onCreateTrip = {}, onSelectTrip = {}, onOpenDrawer = {})
        }

        homeScreenRobot(composeTestRule) {
            assertReviewPromptNotDisplayed("Reviewed Trip")
        }
    }

    @Test
    fun clickingReviewPromptTriggersOnReviewTripCallback() = runTest {
        val testScope = TestScope()
        val repository = PackingRepository(FakeDataStoreManager(), testScope)
        val viewModel = PackingViewModel(repository)

        val pastDate = LocalDate(2020, 1, 1)
        repository.addTrip(Trip(id = "t1", title = "Beach Trip", startDate = pastDate, endDate = pastDate, isReviewed = false))

        var reviewedTripId: String? = null
        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onCreateTrip = {},
                onSelectTrip = {},
                onOpenDrawer = {},
                onReviewTrip = { reviewedTripId = it },
            )
        }

        homeScreenRobot(composeTestRule) {
            assertReviewPromptDisplayed("Beach Trip")
            clickReviewPrompt("Beach Trip")
        }

        assert(reviewedTripId == "t1") { "Expected onReviewTrip to be called with trip id 't1' but got $reviewedTripId" }
    }

    @Test
    fun clickingDrawerMenuButtonTriggersOpenDrawerCallback() {
        val testScope = TestScope()
        val repository = PackingRepository(FakeDataStoreManager(), testScope)
        val viewModel = PackingViewModel(repository)
        var drawerOpened = false

        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onCreateTrip = {},
                onSelectTrip = {},
                onOpenDrawer = { drawerOpened = true },
            )
        }

        homeScreenRobot(composeTestRule) {
            openDrawer()
        }

        assert(drawerOpened) { "Expected onOpenDrawer to be called when menu button is clicked" }
    }
}