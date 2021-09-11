package com.nugsky.passphrasegenerator

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.nugsky.passphrasegenerator.util.Generator
import com.nugsky.passphrasegenerator.util.Validator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    private val TAG = "PassphraseGenerator"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val generatorButton = findViewById<Button>(R.id.generatorButton)
        val passphraseTextView = findViewById<TextView>(R.id.passphraseText)
        val numberInputText = findViewById<EditText>(R.id.numberInputText)
        val numberCheckBox = findViewById<CheckBox>(R.id.useNumberCheckBox)
        val capitalizeCheckBox = findViewById<CheckBox>(R.id.capitalizeCheckBox)
        val symbolCheckBox = findViewById<CheckBox>(R.id.useSymbolCheckBox)
        val separatorInputText = findViewById<EditText>(R.id.separatorInputText)

        val copyButton = findViewById<Button>(R.id.copyButton)

        generatorButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                if (!Validator.validateParameters(numberInputText)) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "invalid parameter", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                val separator = if (separatorInputText.text.isEmpty()) "" else separatorInputText.text.toString()[0].toString()
                val wordCount = numberInputText.text.toString().toInt()
                val isCapitalize = capitalizeCheckBox.isChecked
                val isAddNumber = numberCheckBox.isChecked
                val isAddSymbol = symbolCheckBox.isChecked
                val result = getPassphrase(separator, wordCount, isCapitalize, isAddNumber, isAddSymbol)
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

    private suspend fun getPassphrase(separator: String, wordCount: Int, isCapitalize: Boolean, isAddNumber: Boolean, isAddSymbol: Boolean): String {
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
        return Generator.getPassphrase(words, separator, wordCount, isCapitalize, isAddNumber, isAddSymbol)
    }
}