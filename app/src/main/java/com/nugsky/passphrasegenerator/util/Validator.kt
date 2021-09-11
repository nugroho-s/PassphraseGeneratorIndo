package com.nugsky.passphrasegenerator.util

import android.widget.EditText

class Validator {
    companion object {
        fun validateParameters(wordCount: EditText): Boolean {
            return !wordCount.text.isEmpty() && wordCount.text.toString().toInt() > 0
        }
    }
}