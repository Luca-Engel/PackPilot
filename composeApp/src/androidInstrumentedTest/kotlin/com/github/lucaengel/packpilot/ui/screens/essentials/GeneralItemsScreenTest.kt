package com.github.lucaengel.packpilot.ui.screens.essentials

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.lucaengel.packpilot.repository.FakeDataStoreManager
import com.github.lucaengel.packpilot.repository.PackingRepository
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class GeneralItemsScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun addingNewEssentialItemDisplaysInList() =
        runTest {
            val repository = PackingRepository(FakeDataStoreManager(), backgroundScope)
            val viewModel = PackingViewModel(repository)

            composeTestRule.setContent {
                GeneralItemsScreen(
                    viewModel = viewModel,
                    onBack = {},
                )
            }

            generalItemsScreenRobot(composeTestRule) {
                clickAddEssential()
                // Use a unique name to avoid collisions with repository mock data
                enterEssentialName("Wool Socks")
                enterEssentialQuantity("5")
                clickConfirmAdd()

                assertEssentialExists("Wool Socks")
                assertQuantity("Wool Socks", 5)
            }
        }

    @Test
    fun increasingQuantityUpdatesDisplay() =
        runTest {
            val repository = PackingRepository(FakeDataStoreManager(), backgroundScope)
            val viewModel = PackingViewModel(repository)
            viewModel.addGeneralItem("T-Shirt", 3, false)

            composeTestRule.setContent {
                GeneralItemsScreen(
                    viewModel = viewModel,
                    onBack = {},
                )
            }

            generalItemsScreenRobot(composeTestRule) {
                assertQuantity("T-Shirt", 3)
                clickIncreaseQuantity("T-Shirt")
                assertQuantity("T-Shirt", 4)
            }
        }

    @Test
    fun deletingEssentialItemRemovesItFromList() =
        runTest {
            val repository = PackingRepository(FakeDataStoreManager(), backgroundScope)
            val viewModel = PackingViewModel(repository)
            viewModel.addGeneralItem("Jeans", 2, false)

            composeTestRule.setContent {
                GeneralItemsScreen(
                    viewModel = viewModel,
                    onBack = {},
                )
            }

            generalItemsScreenRobot(composeTestRule) {
                assertEssentialExists("Jeans")
                clickDeleteEssential("Jeans")

                composeTestRule.onNodeWithTag("BaseItemRow_Jeans").assertDoesNotExist()
            }
        }
}
