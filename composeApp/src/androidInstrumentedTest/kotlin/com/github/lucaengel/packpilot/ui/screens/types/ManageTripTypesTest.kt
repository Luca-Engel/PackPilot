package com.github.lucaengel.packpilot.ui.screens.types

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.lucaengel.packpilot.repository.FakeDataStoreManager
import com.github.lucaengel.packpilot.repository.PackingRepository
import com.github.lucaengel.packpilot.viewmodel.PackingViewModel
import kotlinx.coroutines.test.TestScope
import org.junit.Rule
import org.junit.Test

class ManageTripTypesTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun creatingNewTripTypeAndAddingItem() {
        val testScope = TestScope()
        val repository = PackingRepository(FakeDataStoreManager(), testScope)
        val viewModel = PackingViewModel(repository)

        composeTestRule.setContent {
            ManageTripTypesScreen(
                viewModel = viewModel,
                onBack = {}
            )
        }

        manageTripTypesRobot(composeTestRule) {
            clickNewType()
            enterTypeName("Skiing")
            clickCreateType()
            
            assertTypeExists("Skiing")
            selectType("Skiing")
            
            clickAddItemToType()
            enterItemName("Skis")
            clickAdd()
            
            assertItemInType("Skis")
        }
    }
}
