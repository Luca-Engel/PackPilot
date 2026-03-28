package com.github.lucaengel.packpilot.viewmodel

import com.github.lucaengel.packpilot.model.ItemCategory
import com.github.lucaengel.packpilot.model.ItemSource
import com.github.lucaengel.packpilot.repository.FakeDataStoreManager
import com.github.lucaengel.packpilot.repository.PackingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TemplatePersistenceTest {

    @Test
    fun savingTripAsTemplateCreatesTemplateWithCorrectName() = runTest {
        val repository = PackingRepository(FakeDataStoreManager(), backgroundScope)
        val viewModel = PackingViewModel(repository)

        val start = LocalDate(2025, 6, 1)
        val end = LocalDate(2025, 6, 5)
        viewModel.createTrip("Beach Holiday", "city", start, end)
        val tripId = viewModel.trips.value.keys.first()

        viewModel.saveCurrentTripAsTemplate(tripId, "Beach Essentials")

        val templates = viewModel.templates.value
        assertEquals(1, templates.size)
        assertEquals("Beach Essentials", templates.values.first().name)
    }

    @Test
    fun savingTripAsTemplatePreservesItemNamesQuantitiesAndCategories() = runTest {
        val repository = PackingRepository(FakeDataStoreManager(), backgroundScope)
        val viewModel = PackingViewModel(repository)

        val start = LocalDate(2025, 6, 1)
        val end = LocalDate(2025, 6, 5)
        viewModel.addGeneralItem("T-Shirt", 3, false, ItemCategory.CLOTHING)
        viewModel.createTrip("Summer Trip", "city", start, end)
        val tripId = viewModel.trips.value.keys.first()

        viewModel.saveCurrentTripAsTemplate(tripId, "Summer Template")

        val templateItems = viewModel.templates.value.values.first().items
        assertEquals(1, templateItems.size)
        val item = templateItems.first()
        assertEquals("T-Shirt", item.name)
        assertEquals(3, item.quantity)
        assertEquals(ItemCategory.CLOTHING, item.category)
    }

    @Test
    fun savingTripAsTemplateRecordsEffectiveItemSource() = runTest {
        val repository = PackingRepository(FakeDataStoreManager(), backgroundScope)
        val viewModel = PackingViewModel(repository)

        val start = LocalDate(2025, 6, 1)
        val end = LocalDate(2025, 6, 3)
        viewModel.addGeneralItem("Sunscreen", 1, false, ItemCategory.OTHER)
        viewModel.createTrip("Summer Trip", "city", start, end)
        val tripId = viewModel.trips.value.keys.first()

        viewModel.saveCurrentTripAsTemplate(tripId, "Summer Template")

        val templateItem = viewModel.templates.value.values.first().items.first()
        assertEquals(ItemSource.ESSENTIAL, templateItem.source)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun savedTemplateIsPersistedAcrossRepositoryRestart() = runTest {
        val dataStoreManager = FakeDataStoreManager()
        val repository1 = PackingRepository(dataStoreManager, backgroundScope)
        val viewModel1 = PackingViewModel(repository1)

        val start = LocalDate(2025, 7, 1)
        val end = LocalDate(2025, 7, 4)
        viewModel1.addGeneralItem("Passport", 1, false, ItemCategory.DOCUMENTS)
        viewModel1.createTrip("City Break", "city", start, end)
        val tripId = viewModel1.trips.value.keys.first()
        viewModel1.saveCurrentTripAsTemplate(tripId, "City Break Template")

        // Simulate app restart: new repository wired to the same DataStore
        val repository2 = PackingRepository(dataStoreManager, backgroundScope)
        val viewModel2 = PackingViewModel(repository2)

        // Wait for the init coroutine to load persisted templates from the DataStore
        val loadedTemplates = viewModel2.templates.filter { it.isNotEmpty() }.first()
        assertEquals(1, loadedTemplates.size)
        assertEquals("City Break Template", loadedTemplates.values.first().name)
    }

    @Test
    fun deletingTemplateRemovesItFromState() = runTest {
        val repository = PackingRepository(FakeDataStoreManager(), backgroundScope)
        val viewModel = PackingViewModel(repository)

        val start = LocalDate(2025, 8, 1)
        val end = LocalDate(2025, 8, 5)
        viewModel.createTrip("Weekend Hike", "city", start, end)
        val tripId = viewModel.trips.value.keys.first()
        viewModel.saveCurrentTripAsTemplate(tripId, "Hike Template")

        val templateId = viewModel.templates.value.keys.first()
        viewModel.deleteTemplate(templateId)

        assertTrue(viewModel.templates.value.isEmpty())
    }

    @Test
    fun multipleTemplatesCanBeStoredIndependently() = runTest {
        val repository = PackingRepository(FakeDataStoreManager(), backgroundScope)
        val viewModel = PackingViewModel(repository)

        val start = LocalDate(2025, 6, 1)
        val end = LocalDate(2025, 6, 5)
        viewModel.createTrip("Trip A", "city", start, end)
        val tripIdA = viewModel.trips.value.keys.first()
        viewModel.saveCurrentTripAsTemplate(tripIdA, "Template A")

        viewModel.createTrip("Trip B", "city", start, end)
        val tripIdB = viewModel.trips.value.keys.first { it != tripIdA }
        viewModel.saveCurrentTripAsTemplate(tripIdB, "Template B")

        val templateNames = viewModel.templates.value.values.map { it.name }.toSet()
        assertEquals(setOf("Template A", "Template B"), templateNames)
    }
}
