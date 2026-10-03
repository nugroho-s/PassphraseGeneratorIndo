package com.nugsky.passphrasegenerator

import com.nugsky.passphrasegenerator.util.Generator
import java.text.Normalizer
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

class DictionaryTest {
    private fun bundledWords(): List<String> = requireNotNull(
        javaClass.classLoader?.getResourceAsStream("words_list.txt")
    ) { "Bundled dictionary is missing from test resources" }
        .bufferedReader(Charsets.UTF_8).use { it.readLines() }

    @Test fun dictionaryContainsExpectedUniqueSortedWords() {
        val words = bundledWords()
        assertEquals(42_816, words.size)
        assertEquals(words.size, words.toSet().size)
        assertEquals(words.sorted(), words)
    }

    @Test fun entriesAreNormalizedLowercaseAlphabeticWordsOfSupportedLength() {
        for (word in bundledWords()) {
            assertTrue("Invalid word length: $word", word.length in 3..16)
            assertTrue("Nonalphabetic entry: $word", word.all { it.isLetter() })
            assertEquals(word, word.lowercase(Locale.ROOT))
            assertTrue("Non-normalized entry: $word", Normalizer.isNormalized(word, Normalizer.Form.NFC))
        }
    }

    @Test fun generatorUsesBundledDictionaryWithSixWordDefault() {
        val words = bundledWords()
        val dictionary = words.toSet()
        val generator = Generator(words)
        repeat(10) {
            val selected = generator.getPassphrase("-", 6, false, false, false).split("-")
            assertEquals(6, selected.size)
            assertTrue(selected.all { it in dictionary })
        }
    }

    @Test fun dictionaryAttributionAndLicensesAreBundled() {
        for (file in listOf("NOTICE.txt", "LGPL-3.0.txt", "GPL-3.0.txt")) {
            val text = requireNotNull(javaClass.classLoader?.getResourceAsStream("dictionary_licenses/$file"))
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            assertTrue("Empty attribution/license: $file", text.isNotBlank())
        }
    }
}
