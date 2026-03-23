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
 * 3. Replace multiple internal spaces with a single
 */
class DefaultNameNormalizer : NameNormalizer {
    override fun normalize(name: String): String =
        name
            .trim()
            .lowercase()
            .replace(Regex("\\s+"), " ")
}
