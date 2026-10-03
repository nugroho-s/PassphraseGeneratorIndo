package com.nugsky.passphrasegenerator.util

import java.security.SecureRandom

/** Uniform, independent draws with replacement from a normalized, unique dictionary. */
class Generator(wordList: List<String>, private val random: SecureRandom = SecureRandom()) {
    private val words = wordList.map { it.trim() }.filter { it.isNotEmpty() }.distinct()

    init {
        require(words.size >= 2) { "The dictionary must contain at least two unique words." }
    }

    fun getPassphrase(
        separator: String,
        wordCount: Int,
        isCapitalize: Boolean,
        isAddNumber: Boolean,
        isAddSymbol: Boolean
    ): String {
        require(wordCount in Validator.MIN_WORD_COUNT..Validator.MAX_WORD_COUNT) {
            "Word count is outside the supported range."
        }
        require(Validator.isValidSeparator(separator)) { "Invalid separator." }

        val selectedWords = MutableList(wordCount) { words[random.nextInt(words.size)] }
        if (isCapitalize) {
            selectedWords.indices.forEach { index ->
                selectedWords[index] = selectedWords[index].replaceFirstChar { it.uppercaseChar() }
            }
        }
        if (isAddNumber) {
            val index = random.nextInt(wordCount)
            selectedWords[index] += random.nextInt(10).toString()
        }
        if (isAddSymbol) {
            val index = random.nextInt(wordCount)
            selectedWords[index] += SYMBOLS[random.nextInt(SYMBOLS.length)]
        }
        return selectedWords.joinToString(separator)
    }

    companion object {
        // All 32 ASCII punctuation characters, each appearing exactly once.
        const val SYMBOLS = "!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
    }
}
