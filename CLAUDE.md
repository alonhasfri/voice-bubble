# Voice Bubble

Free Android voice typing: a floating mic bubble that shows while a keyboard is open, turns speech
into text on the phone, and types it into any app.

Full history, decisions and to-dos: `memory/MEMORY-BOOK.md` in the private repo
`alonhasfri/shared-project` (section 2).

## Key facts

- Speech to text runs on the phone: Whisper (whisper.cpp v1.7.6, two native builds, "base" and
  "fast" with i8mm, picked at runtime, flash attention on) and the sherpa-onnx streaming "Live"
  engine. The voice never leaves the phone.
- AI features (characters, rewrite, translate, reply, summarise, fixing mistakes) use the user's
  own key: OpenRouter `sk-or-` (usual, free), Claude `sk-ant`, or Gemini.
- Two APKs:
  - `voice-bubble.apk` (basic): copies the text to paste, no accessibility service.
  - `voice-bubble-autotype.apk`: accessibility service "Voice Bubble: type into apps". This is the
    one the owner uses.
- Windows version in `desktop/` (its own Gradle build, Kotlin/JVM + Swing, no Android SDK): it compiles the
  phone's shared code files from `app/src/main/java` (list in `desktop/build.gradle.kts`) on top of a tiny
  `android.content.Context`/`SharedPreferences` stand-in. Keep those shared files free of other Android APIs (Android-only
  drawing for skins lives in `SkinDraw.kt`). whisper.cpp is built as `voicebubble_base.dll` from `desktop/native`.
  Bubble = always-on-top window, Ctrl+Shift+Space hotkey (JNativeHook), types by clipboard + Ctrl+V (JNA tracks the
  target window). `VoiceBubble --transcribe file.wav` prints the result (handy for testing on Linux with xvfb).
  Downloads: `releases/latest/download/VoiceBubble-Setup.exe` (installer) and `VoiceBubble-Windows.zip`.
- Processing order: voice commands → emoji by name → filler remover → numbers & dates → style per
  app → dictionary/word packs → shortcuts → AI fixes mistakes → character → translation →
  Roblox-safe words → length limit.
- CI `.github/workflows/build.yml` runs on every push to `main`: NDK 26.3.11579264, CMake 3.22.1,
  sherpa-onnx 1.13.5 AAR, unit tests, both APKs; a `windows` job (windows-2022, jpackage); then `publish` makes ONE
  release `voice-bubble-<N>` with the APKs + Windows files, marked Latest.
  Version = run number + 100, which continues the builds from combo-checker (up to 63).
- It publishes only when the `SIGNING_KEYSTORE_B64` secret is set. It must stay the same key, or
  installs stop updating. Never commit it.
- The `models` release hosts `ggml-distil-small.en-q8_0.bin`.
- Moved here from combo-checker on 15 Nov 2026. The old releases there are kept, and build 63 there
  points the update check here.

## Standing rules

- Long messages are shortened into one message, never split.
- Royal mode (1500s royal) replaces Shakespeare, with no "talking to peasants".
- Characters must keep the user's meaning and words. Never type random text when the AI fails;
  fall back to the instant character or the user's own words.
- Keep the unit tests passing (CI runs them before building).
