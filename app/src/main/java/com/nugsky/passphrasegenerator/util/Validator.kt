package com.nugsky.passphrasegenerator.util

object Validator {
    const val MIN_WORD_COUNT = 4
    const val MAX_WORD_COUNT = 24
    const val MAX_SEPARATOR_LENGTH = 16

    fun parseWordCount(value: String): Int? = value.trim().toIntOrNull()
        ?.takeIf { it in MIN_WORD_COUNT..MAX_WORD_COUNT }

    fun isValidSeparator(value: String): Boolean =
        value.length <= MAX_SEPARATOR_LENGTH && value.none { it.isISOControl() }
}
