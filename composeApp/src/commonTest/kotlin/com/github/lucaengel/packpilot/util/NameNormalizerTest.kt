package com.github.lucaengel.packpilot.util

import kotlin.test.Test
import kotlin.test.assertEquals

class NameNormalizerTest {
    private val normalizer: NameNormalizer = DefaultNameNormalizer()

    @Test
    fun normalize_trimsLeadingAndTrailingWhitespaces() {
        val input = "  Socks  "
        val expected = "socks"
        assertEquals(expected, normalizer.normalize(input))
    }

    @Test
    fun normalize_convertsToLowercase() {
        val input = "PHONE Charger"
        val expected = "phone charger"
        assertEquals(expected, normalizer.normalize(input))
    }

    @Test
    fun normalize_reducesMultipleInternalSpacesToSingleSpace() {
        val input = "travel    adapter"
        val expected = "travel adapter"
        assertEquals(expected, normalizer.normalize(input))
    }

    @Test
    fun normalize_handlesCombinationOfTrimmingCasingAndSpaceNormalization() {
        val input = "   Extra    SPACES   "
        val expected = "extra spaces"
        assertEquals(expected, normalizer.normalize(input))
    }

    @Test
    fun normalize_returnsEmptyStringForWhitespaceOnlyInput() {
        val input = "     "
        val expected = ""
        assertEquals(expected, normalizer.normalize(input))
    }

    @Test
    fun normalize_returnsEmptyStringForEmptyInput() {
        val input = ""
        val expected = ""
        assertEquals(expected, normalizer.normalize(input))
    }

    @Test
    fun normalize_doesNotChangeAlreadyNormalizedStrings() {
        val input = "toothbrush"
        val expected = "toothbrush"
        assertEquals(expected, normalizer.normalize(input))
    }

    @Test
    fun normalize_handlesNamesWithMultipleWordsCorrectly() {
        val input = "  Power   Bank  "
        val expected = "power bank"
        assertEquals(expected, normalizer.normalize(input))
    }
}
