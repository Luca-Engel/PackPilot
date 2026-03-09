package com.github.lucaengel.packpilot.ui.screens.essentials

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

class CategoryUiTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun addingEssentialWithCategoryDisplaysCorrectly() = runTest {
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
            enterEssentialName("Laptop")
            selectCategoryInDialog(ItemCategory.ELECTRONICS)
            clickConfirmAdd()

            assertEssentialExists("Laptop")
            assertCategory("Laptop", ItemCategory.ELECTRONICS)
        }
    }

    @Test
    fun changingCategoryInListUpdatesDisplay() = runTest {
        val repository = PackingRepository(FakeDataStoreManager(), backgroundScope)
        val viewModel = PackingViewModel(repository)
        viewModel.addGeneralItem("Apple", 1, false, ItemCategory.FOOD)

        composeTestRule.setContent {
            GeneralItemsScreen(
                viewModel = viewModel,
                onBack = {},
            )
        }

        generalItemsScreenRobot(composeTestRule) {
            assertEssentialExists("Apple")
            assertCategory("Apple", ItemCategory.FOOD)

            changeCategory("Apple", ItemCategory.OTHER)
            assertCategory("Apple", ItemCategory.OTHER)
        }
    }
}
