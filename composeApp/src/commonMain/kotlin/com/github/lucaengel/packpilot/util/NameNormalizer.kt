package com.github.lucaengel.packpilot.util

/**
 * Interface for normalizing strings to a comparable form.
 */
interface NameNormalizer {
    /**
     * Normalizes the given [name].
     *
     * @param name The string to normalize.
     * @return The normalized string.
     */
    fun normalize(name: String): String
}

/**
 * Default implementation of [NameNormalizer].
 *
 * Normalization Steps:
 * 1. Trim leading and trailing whitespaces.
 * 2. Convert to lowercase.
 * 3. Replace multiple internal whitespace characters with a single space.
 */
class DefaultNameNormalizer : NameNormalizer {

    companion object {
        private val WHITESPACE_REGEX = Regex("\\s+")
    }

    override fun normalize(name: String): String =
        name
            .trim()
            .lowercase()
            .replace(WHITESPACE_REGEX, " ")
}
