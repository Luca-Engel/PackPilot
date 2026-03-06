package com.github.lucaengel.packpilot.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelsTest {

    @Test
    fun testTripDaysCalculation() {
        val startDate = LocalDate(2024, 1, 1)
        val endDate = LocalDate(2024, 1, 7) // 7 days inclusive
        val trip = Trip(
            id = "1",
            title = "Test Trip",
            startDate = startDate,
            endDate = endDate
        )
        
        assertEquals(7, trip.days, "Trip duration should be 7 days")
    }

    @Test
    fun testTripDaysCalculationSingleDay() {
        val date = LocalDate(2024, 1, 1)
        val trip = Trip(
            id = "1",
            title = "Test Trip",
            startDate = date,
            endDate = date
        )
        
        assertEquals(1, trip.days, "Single day trip should have duration 1")
    }

    @Test
    fun testTripDaysMinimumOne() {
        val startDate = LocalDate(2024, 1, 10)
        val endDate = LocalDate(2024, 1, 1) // End before start
        val trip = Trip(
            id = "1",
            title = "Test Trip",
            startDate = startDate,
            endDate = endDate
        )
        
        assertEquals(1, trip.days, "Trip days should default to at least 1 even if end is before start")
    }
}
