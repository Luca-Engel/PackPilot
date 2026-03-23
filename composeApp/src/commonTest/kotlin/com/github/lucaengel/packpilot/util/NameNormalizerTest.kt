package com.github.lucaengel.packpilot.util

import kotlin.test.Test
import kotlin.test.assertEquals

class NameNormalizerTest {
    private val normalizer: NameNormalizer = DefaultNameNormalizer()

    @Test
    fun normalizeTrimsLeadingAndTrailingWhitespaces() {
        val input = "  Socks  "
        val expected = "socks"
        assertEquals(expected, normalizer.normalize(input))
    }

    @Test
    fun normalizeConvertsToLowercase() {
        val input = "PHONE Charger"
        val expected = "phone charger"
        assertEquals(expected, normalizer.normalize(input))
    }

    @Test
    fun normalizeReducesMultipleInternalSpacesToSingleSpace() {
        val input = "travel    adapter"
        val expected = "travel adapter"
        assertEquals(expected, normalizer.normalize(input))
    }

    @Test
    fun normalizeHandlesCombinationOfTrimmingCasingAndSpaceNormalization() {
        val input = "   Extra    SPACES   "
        val expected = "extra spaces"
        assertEquals(expected, normalizer.normalize(input))
    }

    @Test
    fun normalizeReturnsEmptyStringForWhitespaceOnlyInput() {
        val input = "     "
        val expected = ""
        assertEquals(expected, normalizer.normalize(input))
    }

    @Test
    fun normalizeReturnsEmptyStringForEmptyInput() {
        val input = ""
        val expected = ""
        assertEquals(expected, normalizer.normalize(input))
    }

    @Test
    fun normalizeDoesNotChangeAlreadyNormalizedStrings() {
        val input = "toothbrush"
        val expected = "toothbrush"
        assertEquals(expected, normalizer.normalize(input))
    }

    @Test
    fun normalizeHandlesNamesWithMultipleWordsCorrectly() {
        val input = "  Power   Bank  "
        val expected = "power bank"
        assertEquals(expected, normalizer.normalize(input))
    }
}
