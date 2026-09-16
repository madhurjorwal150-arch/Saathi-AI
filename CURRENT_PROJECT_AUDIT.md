# Current Project Audit — Sathi AI

## System State
- Canonical Root: `Saathi-AI/`
- Target SDK: 34 (Android 14)
- Current Build State: Compiles successfully via GitHub Actions (`app-debug.apk`)
- UI Stack: Jetpack Compose Material3 (Dark Theme `#080B12`)

## Architecture Components
- `MainActivity.kt`: Handles lockscreen window flags, permission launcher, and Compose rendering.
- `VoiceListeningService.kt`: Foreground service wrapping Android `SpeechRecognizer`.
- `SathiIntentClassifier.kt`: Regex/string matching intent router.
- `FullActionExecutor.kt`: Direct Android OS command executor (Torch, Volume, App Launch, Clock).
- `TtsManager.kt`: Android `TextToSpeech` engine (Hindi/English).

## Identified Technical Gaps
1. State Management: In-memory lists used for notes and chat history; no offline DB persistence.
2. Voice Loop: TTS audio output bleeds into microphone input causing echo triggers.
3. Spotify & YouTube: Playback success claimed without verification; only launches search intents.
4. Assistant Role: Standard foreground service used instead of `VoiceInteractionService`.
