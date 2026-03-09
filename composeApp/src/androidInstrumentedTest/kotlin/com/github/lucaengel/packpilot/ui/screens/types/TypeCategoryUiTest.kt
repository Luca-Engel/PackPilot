package com.github.lucaengel.packpilot.ui.screens.types

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.lucaengel.packpilot.model.ItemCategory
import com.github.lucaengel.packpilot.repository.FakeDataStoreManager
import com.github.lucaengel.packpilot.repository.PackingRepository
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class TypeCategoryUiTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun addingItemToTypeWithCategoryDisplaysCorrectly() =
        runTest {
            val repository = PackingRepository(FakeDataStoreManager(), backgroundScope)
            val viewModel = PackingViewModel(repository)
            viewModel.createNewTripType("Skiing")

            composeTestRule.setContent {
                ManageTripTypesScreen(
                    viewModel = viewModel,
                    onBack = {},
                )
            }

            manageTripTypesRobot(composeTestRule) {
                selectType("Skiing")
                clickAddItemToType()
                enterItemName("Helmet")
                selectCategoryInAddDialog(ItemCategory.EQUIPMENT)
                clickAdd()

                assertItemInType("Helmet")
                assertCategory("Helmet", ItemCategory.EQUIPMENT)
            }
        }

    @Test
    fun changingCategoryInTypeUpdatesDisplay() =
        runTest {
            val repository = PackingRepository(FakeDataStoreManager(), backgroundScope)
            val viewModel = PackingViewModel(repository)
            viewModel.createNewTripType("Hiking")
            val listId =
                viewModel.lists.value.values
                    .find { it.title == "Hiking" }!!
                    .id
            viewModel.addItemToTripType(listId, "Boots", 1, false, ItemCategory.CLOTHING)

            composeTestRule.setContent {
                ManageTripTypesScreen(
                    viewModel = viewModel,
                    onBack = {},
                )
            }

            manageTripTypesRobot(composeTestRule) {
                selectType("Hiking")
                assertCategory("Boots", ItemCategory.CLOTHING)

                changeCategory("Boots", ItemCategory.EQUIPMENT)
                assertCategory("Boots", ItemCategory.EQUIPMENT)
            }
        }

    @Test
    fun selectingTripTypeClosesSidebar() =
        runTest {
            val repository = PackingRepository(FakeDataStoreManager(), backgroundScope)
            val viewModel = PackingViewModel(repository)
            viewModel.createNewTripType("Skiing")

            composeTestRule.setContent {
                ManageTripTypesScreen(
                    viewModel = viewModel,
                    onBack = {},
                )
            }

            manageTripTypesRobot(composeTestRule) {
                // Sidebar is visible by default
                assertSidebarVisible()

                selectType("Skiing")

                // Sidebar should be hidden after selection
                assertSidebarHidden()

                // Can be toggled back
                toggleSidebar()
                assertSidebarVisible()
            }
        }
}
