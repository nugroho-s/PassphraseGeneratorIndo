package com.nugsky.passphrasegenerator

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private val TAG = "PassphraseGenerator"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val generatorButton = findViewById<Button>(R.id.generatorButton)
        val passphraseTextView = findViewById<TextView>(R.id.passphraseText)
        val numberInputText = findViewById<EditText>(R.id.numberInputText)
        val numberCheckBox = findViewById<CheckBox>(R.id.useNumberCheckBox)

        val copyButton = findViewById<Button>(R.id.copyButton)

        generatorButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val isAddNumber = numberCheckBox.isChecked
                val wordCount = numberInputText.text.toString().toInt()
                val result = getPassphrase(wordCount, isAddNumber)
                withContext(Dispatchers.Main) {
                    passphraseTextView.text = result
                }
            }
        }

        copyButton.setOnClickListener {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Copied Text", passphraseTextView.text)
            clipboardManager.setPrimaryClip(clipData)
        }
    }

    private suspend fun getPassphrase(wordCount: Int, isAddNumber: Boolean): String {
        val reader : BufferedReader
        var words: List<String> = ArrayList<String>()
        try {
            reader = BufferedReader(
                    InputStreamReader(assets.open("words_list.txt"))
            )
            words = reader.readLines()
        } catch (e: IOException) {
            Log.e(TAG, "getPassphrase exception", e)
        }
        if (words.isEmpty())
            return ""
        val selectedWords = getRandomSubList(words, wordCount) as ArrayList<String>
        if (isAddNumber) {
            val i = Random.nextInt(selectedWords.size)
            selectedWords[i] = selectedWords[i] + "1"
        }
        return selectedWords.joinToString("-")
    }

    private suspend fun getRandomSubList(words: List<Any>, num: Int): List<Any> {
        val subList = ArrayList<Any>()
        for (i in 1..num) {
            subList.add(words.random())
        }
        return subList
    }
}