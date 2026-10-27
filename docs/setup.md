# MajibuAI Android Codelab Setup

**Ask. Understand. Remember.**

MajibuAI is an AI learning companion built with Kotlin and Jetpack Compose. In this codelab, you’ll explore how to route chat requests between cloud and on-device AI providers while keeping the chat UI and repository flow consistent.

## What you’ll explore

- Firebase AI Logic with Gemini
- OpenRouter as an alternative cloud provider
- Streaming responses with Kotlin Flow
- On-device Gemma inference with LiteRT-LM
- Hybrid routing between cloud and local providers
- Typed error handling and retry states
- Saving questions and answers to the Library

## Requirements

- Android Studio with support for Android Gradle Plugin 9.1.1
- JDK 17
- Android SDK Platform 37
- An emulator or Android device running API 24 or higher
- Git
- Internet access for dependency resolution and the initial on-device model download
- A Firebase project for Firebase AI Logic
- An OpenRouter API key

A physical device is recommended when exploring on-device inference.

## Get the project

```bash
git clone https://github.com/valentineRutto/MajibuAI.git
cd MajibuAI
git fetch --all
```

To follow the codelab from its starter checkpoint:

```bash
git switch 00-starter
```

To explore the completed application:

```bash
git switch main
```

The repository checkpoints are:

| Branch | Codelab focus |
|---|---|
| `00-starter` | Starting app and chat UI |
| `01-cloud-ai` | Firebase AI Logic |
| `02-multi-provider` | Provider abstraction and OpenRouter |
| `03-streaming` | Streaming chat responses |
| `04-on-device-ai` | LiteRT-LM and local model management |
| `o5-hybrid-ai` | Cloud and on-device routing |
| `main` | Completed reference application |

## Open and sync the project

1. Open Android Studio.
2. Select **Open** and choose the cloned `MajibuAI` folder.
3. Allow Android Studio to create `local.properties` and sync Gradle.
4. Install Android SDK Platform 37 if prompted.
5. Set Android Studio's Gradle JDK to JDK 17 if Gradle sync reports a Java version error.

## Configure OpenRouter

The app reads the OpenRouter key from `local.properties` and exposes it through `BuildConfig`.

Add this property to the existing `local.properties` file. Keep the `sdk.dir` entry created by Android Studio:

```properties
OPENROUTER_API_KEY=your_openrouter_api_key
```

Do not commit `local.properties` or share your API key. The app embeds this key in the debug build, so use a development key for this codelab. A production app should keep provider keys on a backend.

## Configure Firebase AI Logic

1. Create or select a Firebase project.
2. Register an Android app with this application ID:

   ```text
   com.valentinerutto.majibuai
   ```

3. In the Firebase console, open **AI Logic** and complete the setup for the Gemini API provider.
4. Download the generated `google-services.json`.
5. Place it here:

   ```text
   app/google-services.json
   ```

   This file is ignored by Git in this repository. Do not commit it.

### Register the App Check debug token

The debug build installs the App Check debug provider. To allow Firebase AI requests from your development device:

1. Run the debug app once.
2. In Logcat, find the `DebugAppCheckProvider` message containing the debug token.
3. In Firebase Console, open **App Check**, select your Android app, and register that token under **Manage debug tokens**.
4. Retry a Firebase AI request.

Keep the debug token private. The release build uses Play Integrity instead of the debug provider.

## Build and run

In Android Studio, select the `app` run configuration and click **Run**.

You can also build the debug APK from a terminal:

```bash
./gradlew :app:assembleDebug
```

## Try the providers

### Firebase

Choose **Gemini** from the provider menu and ask a question, for example:

> Explain Kotlin Flow with a short Android example.

If the request fails, check Logcat and verify your Firebase configuration, App Check token, and project quota.

### OpenRouter

Choose **OpenRouter** and ask the same question. Check that `OPENROUTER_API_KEY` is present in `local.properties` if the request reports a missing or invalid key.

### On-device

1. Choose **On-device**.
2. Use the model download action shown in the screen.
3. Wait for the model to download and initialize.
4. Ask a question after the model is ready.

The model is downloaded from the URL configured in `appModule.kt` and stored in the app’s private files directory. The first download requires an internet connection.

### Hybrid

Choose **Hybrid** and try a question while online. Then try again offline after the on-device model is ready. Hybrid routing can use cloud providers while online and the local model when it is available as a fallback.

### Save an answer

After an answer appears, use the save action and open **Library** to revisit the saved question and answer.

## Follow the request flow

```text
AskScreen
   |
   v
MajibuViewModel
   |
   v
AiRepository
   |             |                 |
   v             v                 v
Firebase      OpenRouter        LiteRT-LM
                                  |
                                  v
                            Local model
```

`AiRepository` selects the provider and returns typed `AiResult` values. `MajibuViewModel` exposes chat and provider state to the Compose UI.

## Troubleshooting

### Gradle reports a Java version error

Set the Gradle JDK in Android Studio to JDK 17, then sync again.

### Firebase reports an App Check or attestation error

Register the debug token printed by the debug build in Firebase Console. Confirm that the app is connected to the Firebase project that owns the registered Android app.

### Firebase reports a quota error

Check the model and project limits in Firebase AI Logic or Google AI Studio. Switching providers will let you continue the codelab if another provider is configured.

### OpenRouter reports a missing key or 401

Check that `OPENROUTER_API_KEY` is in the project-root `local.properties` file, without quotes or extra spaces, and that the key is active.

### On-device model is unavailable or fails to initialize

Confirm that the download completed, that the device has enough free storage, and that the device remains online during the initial download. Check Logcat for LiteRT-LM errors.

## Project map

| Path | Purpose |
|---|---|
| `ui/ask/AskScreen.kt` | Chat interface and provider selection |
| `ui/MajibuViewModel.kt` | Chat state, provider selection, and user actions |
| `data/ai/AiRepository.kt` | Provider routing and result handling |
| `data/ai/firebase/FirebaseGeminiEngine.kt` | Firebase AI Logic integration |
| `data/ai/openrouter/` | OpenRouter API and engine |
| `data/ai/ondevice/` | LiteRT-LM model download and inference |
| `di/appModule.kt` | Koin dependency registration |

## Continue the codelab

Open [`codelab.md`](codelab.md) for the guided implementation steps.

---

**MajibuAI — Ask. Understand. Remember.**
