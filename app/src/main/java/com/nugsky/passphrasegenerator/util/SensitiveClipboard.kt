package com.nugsky.passphrasegenerator.util

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.os.Build
import android.os.PersistableBundle
import android.os.SystemClock
import java.util.UUID

/** Keeps only ownership metadata in memory, never a second copy of the passphrase. */
object SensitiveClipboard {
    private var ownedLabel: String? = null
    private var expiresAt = 0L
    const val TIMEOUT_MILLIS = 60_000L

    fun copy(clipboard: ClipboardManager, passphrase: String) {
        val label = "PassphraseGenerator:${UUID.randomUUID()}"
        val clip = ClipData.newPlainText(label, passphrase)
        clip.description.extras = PersistableBundle().apply {
            val sensitiveKey = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ClipDescription.EXTRA_IS_SENSITIVE
            } else {
                "android.content.extra.IS_SENSITIVE"
            }
            putBoolean(sensitiveKey, true)
        }
        clipboard.setPrimaryClip(clip)
        ownedLabel = label
        expiresAt = SystemClock.elapsedRealtime() + TIMEOUT_MILLIS
    }

    fun remainingMillis(): Long? = ownedLabel?.let {
        (expiresAt - SystemClock.elapsedRealtime()).coerceAtLeast(0)
    }

    // Call only while the app has focus: Android restricts background clipboard access.
    fun clearIfExpired(clipboard: ClipboardManager) {
        if (ownedLabel == null || SystemClock.elapsedRealtime() < expiresAt) return
        clearOwned(clipboard)
    }

    fun clearOwned(clipboard: ClipboardManager) {
        val label = ownedLabel ?: return
        if (clipboard.primaryClipDescription?.label?.toString() == label) {
            clipboard.clearPrimaryClip()
        }
        ownedLabel = null
        expiresAt = 0L
    }
}
