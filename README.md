# Passphrase Generator Indo

An offline Android app that creates passphrases from the bundled Indonesian dictionary. Requires Android 10 (API 29) or newer. No network permission, account, analytics, or passphrase storage.

## Changes in 2.0

- Cryptographic randomness (`SecureRandom`) for every word, digit, symbol, and placement; words are sampled independently with replacement.
- Six words by default; bounded input of 4–24 words. Empty and multi-character separators work, up to 16 UTF-16 code units. Invalid or overflowing input is rejected without crashing.
- Dictionary loading closes its stream, removes blank entries and duplicates, and caches the dictionary in memory. Generation runs off the UI thread and is cancelled when the screen stops.
- Scrollable Indonesian interface with readable controls, clear action, and system bar/keyboard insets for modern Android.
- Screenshots, screen recording, and recent-app previews are blocked with `FLAG_SECURE`. Generated output is excluded from saved view state and autofill, and is cleared when the app goes into the background or rotates.
- Clipboard copies are marked sensitive. The app clears its own unchanged clipboard entry after 60 seconds while focused, or when focus returns after that deadline. The clear button also clears an owned copy immediately. It never clears an entry copied by another app. Android may clear clips sooner.
- Backups and device transfers exclude app data; cleartext traffic is disabled. Generated passphrases are never logged or written to disk.

The clipboard can still be read by permitted apps and keyboards. Cleanup relies on this app process staying alive and regaining focus; if the process is killed, app-managed cleanup cannot run. Avoid copying on untrusted devices. A Java/Kotlin string cannot be reliably wiped from memory; clearing output removes app references, not every possible memory copy.

The dictionary is inherited from the original repository. Some entries are short, obscure, or contain hyphens; choosing more words helps. Capitalization is deterministic and does not add randomness. Separators and case conversion can make distinct sequences produce the same output, so this app does not claim an exact entropy score. Use a unique passphrase for each account.

## Build

Use JDK 17 and Android SDK platform 36 with build tools 35.0.0. Gradle 8.13 is provided through a checksum-verified wrapper. The project pins AGP 8.13.2, Kotlin 2.3.21, and dependency versions, and resolves libraries from Google Maven and Maven Central. These versions form an API 36 build configuration; newer dependency releases may require newer SDKs and plugins. Lint may report available dependency updates.

References: [AGP compatibility](https://developer.android.com/build/releases/agp-8-13-0-release-notes), [Kotlin releases](https://kotlinlang.org/docs/releases.html), and [Android clipboard guidance](https://developer.android.com/privacy-and-security/risks/secure-clipboard-handling).

Set `JAVA_HOME` and `ANDROID_HOME`, or set `sdk.dir` in an untracked `local.properties` file. Then:

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease
```

Installable debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The release build enables code and resource shrinking. Its APK is unsigned until you configure your own release signing key. Keep keystores and credentials out of Git. If replacing a previous installation, use its original signing key; a debug APK cannot replace an app signed with a different key.

## Persistent development tools on this machine

- JDK: `/home/nugsky/.local/share/android-dev/jdk-17`
- SDK: `/home/nugsky/Android/Sdk`
- Shell environment: `/home/nugsky/.config/android-dev/env.sh`, sourced by `.profile` and `.bashrc`.

New shells load the environment automatically. In an existing shell:

```sh
. ~/.config/android-dev/env.sh
java -version
adb version
```

Manage additional SDK packages with the SDK command-line tools. Android Studio can use the same SDK directory and JDK. The IDE and emulator are separate optional installations.

## Device checks

Unit tests cover generator behavior, every random draw, dictionary normalization, and input boundaries. Android lint checks the manifest, resources, and code. The dictionary instrumentation test requires a connected device:

```sh
./gradlew connectedDebugAndroidTest
```

For manual device testing:

1. Generate with defaults; try 4 and 24 words, blank input, both optional suffixes, capitalization, and empty/multi-character separators.
2. Copy and paste into a trusted field; verify the sensitive clipboard preview and cleanup after 60 seconds with this app focused.
3. Copy another app's text before the timeout; confirm it survives this app's cleanup. Test the clear button with and without an owned clipboard entry.
4. Rotate, background, and reopen the app; confirm output is cleared and Copy is disabled. Try screenshots and recent-app previews.
5. Check small screens, landscape, dark mode, keyboard visibility, and larger text settings.
