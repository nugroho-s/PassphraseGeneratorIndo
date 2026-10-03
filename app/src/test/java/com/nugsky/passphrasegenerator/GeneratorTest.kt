package com.nugsky.passphrasegenerator

import com.nugsky.passphrasegenerator.util.Generator
import com.nugsky.passphrasegenerator.util.Validator
import java.security.SecureRandom
import org.junit.Assert.*
import org.junit.Test

class GeneratorTest {
    private class SequenceRandom(vararg values: Int) : SecureRandom() {
        private val sequence = values.toList().iterator()
        val bounds = mutableListOf<Int>()
        override fun nextInt(bound: Int): Int {
            bounds += bound
            val value = sequence.next()
            require(value in 0 until bound)
            return value
        }
    }

    @Test fun selectsWordsIndependentlyIncludingRepeats() {
        val random = SequenceRandom(0, 1, 0, 1)
        val result = Generator(listOf("satu", "dua"), random)
            .getPassphrase("-", 4, false, false, false)
        assertEquals("satu-dua-satu-dua", result)
        assertEquals(listOf(2, 2, 2, 2), random.bounds)
    }

    @Test fun preservesFullSeparator() {
        val generator = Generator(listOf("satu", "dua"), SequenceRandom(0, 0, 0, 0))
        assertEquals("satu :: satu :: satu :: satu",
            generator.getPassphrase(" :: ", 4, false, false, false))
    }

    @Test fun supportsEmptySeparator() {
        assertEquals("satupadusatupadu", Generator(listOf("satu", "padu"), SequenceRandom(0, 1, 0, 1))
            .getPassphrase("", 4, false, false, false))
    }

    @Test fun normalizesDictionaryWithoutWeightingDuplicates() {
        val random = SequenceRandom(0, 1, 0, 1)
        val generator = Generator(listOf("", " satu ", "dua", "satu", "  "), random)
        assertEquals("satu dua satu dua", generator.getPassphrase(" ", 4, false, false, false))
        assertEquals(listOf(2, 2, 2, 2), random.bounds)
    }

    @Test fun appliesOptionsUsingSecureRandomForEveryDraw() {
        val random = SequenceRandom(0, 1, 0, 1, 2, 9, 3, 0)
        val result = Generator(listOf("satu", "dua"), random)
            .getPassphrase("-", 4, true, true, true)
        assertEquals("Satu-Dua-Satu9-Dua!", result)
        assertEquals(listOf(2, 2, 2, 2, 4, 10, 4, 32), random.bounds)
    }

    @Test fun numberCanBeZeroAtFirstWord() {
        assertEquals("satu0-satu-satu-satu",
            Generator(listOf("satu", "dua"), SequenceRandom(0, 0, 0, 0, 0, 0))
                .getPassphrase("-", 4, false, true, false))
    }

    @Test fun symbolCanBeLastCharacterOfAlphabet() {
        assertEquals("satu-satu-satu-satu~",
            Generator(listOf("satu", "dua"), SequenceRandom(0, 0, 0, 0, 3, 31))
                .getPassphrase("-", 4, false, false, true))
    }

    @Test fun symbolsAreUniqueAsciiPunctuation() {
        val expected = (33..126).map { it.toChar() }.filter { !it.isLetterOrDigit() }.toSet()
        assertEquals(32, Generator.SYMBOLS.length)
        assertEquals(expected, Generator.SYMBOLS.toSet())
    }

    @Test fun secureDefaultProducesSupportedOutput() {
        val words = listOf("satu", "dua", "tiga")
        repeat(20) {
            val result = Generator(words).getPassphrase(" ", 24, false, false, false).split(" ")
            assertEquals(24, result.size)
            assertTrue(result.all { it in words })
        }
    }

    @Test fun rejectsInvalidCountsAtGeneratorBoundary() {
        val generator = Generator(listOf("satu", "dua"))
        for (count in listOf(Int.MIN_VALUE, -1, 0, 1, 3, 25, Int.MAX_VALUE)) {
            assertThrows(IllegalArgumentException::class.java) {
                generator.getPassphrase("-", count, false, false, false)
            }
        }
    }

    @Test fun rejectsUnusableDictionaries() {
        for (words in listOf(emptyList(), listOf(" "), listOf("satu"), listOf("satu", " satu "))) {
            assertThrows(IllegalArgumentException::class.java) { Generator(words) }
        }
    }

    @Test fun rejectsInvalidSeparatorsAtGeneratorBoundary() {
        val generator = Generator(listOf("satu", "dua"))
        for (separator in listOf("a".repeat(17), "\n", "\t", "\u0000")) {
            assertThrows(IllegalArgumentException::class.java) {
                generator.getPassphrase(separator, 6, false, false, false)
            }
        }
    }

    @Test fun parsesOnlyBoundedWordCountsWithoutThrowing() {
        assertEquals(4, Validator.parseWordCount("4"))
        assertEquals(6, Validator.parseWordCount(" 6 "))
        assertEquals(24, Validator.parseWordCount("24"))
        for (value in listOf("", " ", "no", "1.5", "-4", "0", "3", "25", "99999999999999999999")) {
            assertNull(value, Validator.parseWordCount(value))
        }
    }

    @Test fun validatesSeparatorBoundariesAndUnicode() {
        for (value in listOf("", "-", " :: ", "🔒", "a".repeat(16))) {
            assertTrue(Validator.isValidSeparator(value))
        }
        for (value in listOf("a".repeat(17), "\n", "\t", "\u007f")) {
            assertFalse(Validator.isValidSeparator(value))
        }
    }
}
