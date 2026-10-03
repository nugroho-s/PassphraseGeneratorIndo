package com.nugsky.passphrasegenerator

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nugsky.passphrasegenerator.util.Generator
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DictionaryInstrumentedTest {
    @Test fun bundledDictionaryCanGeneratePassphrases() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val words = context.assets.open("words_list.txt").bufferedReader(Charsets.UTF_8)
            .use { it.readLines() }.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        assertTrue(words.size > 20_000)
        val result = Generator(words).getPassphrase(" :: ", 6, false, false, false).split(" :: ")
        assertEquals(6, result.size)
        assertTrue(result.all { it in words })
    }
}
