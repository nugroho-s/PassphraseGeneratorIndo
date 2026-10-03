package com.nugsky.passphrasegenerator

import android.content.ClipboardManager
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.nugsky.passphrasegenerator.util.Generator
import com.nugsky.passphrasegenerator.util.SensitiveClipboard
import com.nugsky.passphrasegenerator.util.Validator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var passphraseText: TextView
    private lateinit var generateButton: Button
    private lateinit var copyButton: Button
    private lateinit var clearButton: Button
    private var generator: Generator? = null
    private var generationJob: Job? = null
    private var clipboardJob: Job? = null
    private val clipboard by lazy { getSystemService(ClipboardManager::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.activity_main)
        val content = findViewById<android.view.View>(R.id.mainContent)
        ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(content)

        passphraseText = findViewById(R.id.passphraseText)
        generateButton = findViewById(R.id.generatorButton)
        copyButton = findViewById(R.id.copyButton)
        clearButton = findViewById(R.id.clearButton)
        val wordCountInput = findViewById<EditText>(R.id.numberInputText)
        val separatorInput = findViewById<EditText>(R.id.separatorInputText)
        val numberOption = findViewById<CheckBox>(R.id.useNumberCheckBox)
        val capitalizeOption = findViewById<CheckBox>(R.id.capitalizeCheckBox)
        val symbolOption = findViewById<CheckBox>(R.id.useSymbolCheckBox)

        generateButton.setOnClickListener {
            // Snapshot all UI state on the main thread before doing background work.
            val wordCount = Validator.parseWordCount(wordCountInput.text.toString())
            if (wordCount == null) {
                wordCountInput.error = getString(R.string.invalid_word_count)
                return@setOnClickListener
            }
            val separator = separatorInput.text.toString()
            if (!Validator.isValidSeparator(separator)) {
                separatorInput.error = getString(R.string.invalid_separator)
                return@setOnClickListener
            }
            val capitalize = capitalizeOption.isChecked
            val addNumber = numberOption.isChecked
            val addSymbol = symbolOption.isChecked
            clearOutput()
            generateButton.isEnabled = false
            generationJob = lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        val activeGenerator = generator ?: assets.open("words_list.txt")
                            .bufferedReader(Charsets.UTF_8).use { Generator(it.readLines()) }
                            .also { generator = it }
                        activeGenerator.getPassphrase(separator, wordCount, capitalize, addNumber, addSymbol)
                    }
                    passphraseText.text = result
                    copyButton.isEnabled = true
                    clearButton.isEnabled = true
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    clearOutput()
                    Toast.makeText(this@MainActivity, R.string.generation_failed, Toast.LENGTH_LONG).show()
                } finally {
                    generateButton.isEnabled = true
                }
            }
        }
        copyButton.setOnClickListener {
            if (!copyButton.isEnabled) return@setOnClickListener
            try {
                SensitiveClipboard.copy(clipboard, passphraseText.text.toString())
                scheduleClipboardCleanup()
                Toast.makeText(this, R.string.copied, Toast.LENGTH_LONG).show()
            } catch (_: SecurityException) {
                Toast.makeText(this, R.string.copy_failed, Toast.LENGTH_SHORT).show()
            }
        }
        clearButton.setOnClickListener {
            clearOutput()
            try {
                SensitiveClipboard.clearOwned(clipboard)
            } catch (_: SecurityException) {
                Toast.makeText(this, R.string.copy_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) scheduleClipboardCleanup() else clipboardJob?.cancel()
    }

    private fun scheduleClipboardCleanup() {
        clipboardJob?.cancel()
        val remaining = SensitiveClipboard.remainingMillis() ?: return
        clipboardJob = lifecycleScope.launch {
            delay(remaining)
            if (window.decorView.hasWindowFocus()) {
                try {
                    SensitiveClipboard.clearIfExpired(clipboard)
                } catch (_: SecurityException) {
                    // Retain ownership metadata so cleanup can retry when focus returns.
                }
            }
        }
    }

    override fun onStop() {
        generationJob?.cancel()
        clipboardJob?.cancel()
        clearOutput()
        super.onStop()
    }

    private fun clearOutput() {
        passphraseText.setText(R.string.passphrase_placeholder)
        copyButton.isEnabled = false
        clearButton.isEnabled = false
    }
}
