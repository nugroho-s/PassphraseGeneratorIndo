package com.nugsky.passphrasegenerator.util

import java.lang.Thread.sleep
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random

class Generator {
    companion object {
        fun getPassphrase(wordList: List<String>,
                                         separator: String,
                                         wordCount: Int,
                                         isCapitalize: Boolean,
                                         isAddNumber: Boolean,
                                         isAddSymbol: Boolean) : String {
            val symbols = "!\"#\$%&\\'()*+,-./:;<=>?@[\\\\]^_`{|}~"

            if (wordList.isEmpty())
                return ""
            val selectedWords = getRandomSubList(wordList, wordCount) as ArrayList<String>

            if (isCapitalize) {
                for (i in 0 until selectedWords.size) {
                    selectedWords[i] = selectedWords[i].replaceFirstChar { it.uppercase() }
                }
            }
            if (isAddNumber) {
                val i = Random.nextInt(selectedWords.size)
                selectedWords[i] = selectedWords[i] + ThreadLocalRandom.current().nextInt(10)
            }
            if (isAddSymbol) {
                val i = Random.nextInt(selectedWords.size)
                val randomSymbol = symbols[ThreadLocalRandom.current().nextInt(symbols.length)]
                selectedWords[i] = selectedWords[i] + randomSymbol
            }

            return selectedWords.joinToString(separator)
        }

        private fun getRandomSubList(words: List<Any>, num: Int): List<Any> {
            val subList = ArrayList<Any>()
            for (i in 1..num) {
                subList.add(words.random())
            }
            return subList
        }
    }
}