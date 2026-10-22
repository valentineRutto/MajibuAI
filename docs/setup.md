
# MajibuAI — Development Setup

**Ask. Understand. Remember.**

Welcome to the MajibuAI Android codelab!

MajibuAI is an AI-powered learning companion that helps users ask questions, understand technical concepts, and save answers for future reference.

This codelab demonstrates how to build an Android application using Kotlin, Jetpack Compose, MVVM architecture, and AI integration.

## Prerequisites

Before getting started, make sure you have:

- Android Studio with Jetpack Compose support
- JDK compatible with the project's Gradle configuration
- Android SDK installed
- Git installed
- An Android emulator or physical Android device
- Basic knowledge of Kotlin and Jetpack Compose

## 1. Clone the Repository

Open your terminal and run:

```bash
git clone https://github.com/valentineRutto/MajibuAI.git
cd MajibuAI
```

## 2. Checkout the Starter Branch

The starter branch contains the initial MajibuAI application.

```bash
git fetch origin
git switch 00-starter
```

If the branch is not available locally, run:

```bash
git switch --track origin/00-starter
```

To work on your own implementation without modifying the starter branch:

```bash
git switch -c my-majibuai-codelab
```

## 3. Open the Project in Android Studio

1. Launch Android Studio.
2. Select **Open**.
3. Navigate to the cloned `MajibuAI` directory.
4. Wait for Gradle synchronization to complete.
5. Install any missing SDK components if prompted.

## 4. Understand the Project Architecture

MajibuAI follows the **Model–View–ViewModel (MVVM)** architecture.

The starter uses:

| Technology | Purpose |
|---|---|
| Kotlin | Application development |
| Jetpack Compose | Declarative UI |
| MVVM | Separation of UI and application logic |
| StateFlow | Reactive UI state management |
| Koin | Dependency injection |
| AiRepository | Handles AI explanation requests |
| Library | Displays saved questions and answers |

### Application Flow

```text
User
  |
  v
AskScreen
  |
  v
AskViewModel
  |
  v
AiRepository
  |
  v
AskUiState
  |
  v
Compose UI
```

The ViewModel manages the screen state while the repository handles explanation requests.

## 5. Run the Application

1. Start an Android emulator or connect a physical device.
2. Select the `app` run configuration.
3. Click **Run** in Android Studio.
4. Wait for the application to launch.

## 6. Explore MajibuAI

### Welcome Screen

The welcome screen introduces MajibuAI and provides a **Get Started** button.

Tap **Get Started** to continue to the main application.

### Ask Screen

The Ask screen allows users to:

- Enter questions about technical concepts.
- Submit questions to MajibuAI.
- View generated or simulated explanations, depending on the repository implementation.
- View loading and error states.
- Save answers for future reference.

### Library Screen

The Library screen allows users to:

- View saved questions and answers.
- Revisit explanations from previous learning sessions.
- Access previously saved content.

The Library is part of the starter implementation and is not an empty placeholder.

## 7. Test the Application

Try the following exercises.

### Test 1: Ask a Question

Enter:

**What is Kotlin Flow?**

Tap **Ask MajibuAI**.

**Expected result:** An explanation appears on the screen.

### Test 2: Save an Answer

After receiving an explanation:

1. Tap the **Save** action.
2. Navigate to the Library screen.
3. Find the saved question and answer.

**Expected result:** The saved answer appears in the Library.

### Test 3: Navigate Between Screens

Navigate between the Ask and Library screens.

**Expected result:** Navigation works without crashing.

## 8. Troubleshooting

### Gradle Sync Fails

- Confirm that your JDK matches the project's requirements.
- Check that the Android SDK is installed.
- Ensure your internet connection is available for dependency downloads.

### Koin Dependency Injection Error

Verify that:

- Koin is initialized in `MajibuApplication`.
- `MajibuApplication` is registered in `AndroidManifest.xml`.
- The repository and ViewModel dependencies are registered in your Koin module.

### No Answer Appears

- Check Logcat for exceptions.
- Verify that `AskViewModel` calls `AiRepository`.
- Confirm that the repository returns a response.
- Check that `AskUiState` is updated correctly.

### Saved Answers Do Not Appear

- Verify that the save action is triggered.
- Confirm that the Library reads from the same data source used to save answers.
- Check that the saved answers are exposed to the Library UI.

## 9. What's Next?

After exploring the starter project, continue through the codelab checkpoints.

| Branch | Focus                                             |
|---|---------------------------------------------------|
| `00-starter` | Compose UI, MVVM, Koin, repository, saved answers |
| `01-cloud-ai` | Cloud AI integration Firebase + Gemini            |
| `02-streaming` | Streaming responses with Kotlin Flow              |
| `03-save-for-later` | Improve or extend saved-answer persistence        |
| `04-on-device-ai` | On-device AI inference                            |
| `05-hybrid-ai` | Cloud and local AI routing                        |
| `06-final` | Final application and polish                      |

## 10. Ready to Build!

You are ready to continue when:

- [ ] The project builds successfully.
- [ ] The welcome screen opens.
- [ ] Navigation works.
- [ ] You can submit a question.
- [ ] MajibuAI displays an explanation.
- [ ] You can save an answer.
- [ ] Saved answers appear in the Library.
- [ ] The application runs without crashes.

**Next step:** Open [`codelab.md`](codelab.md) and continue building MajibuAI.

---

**MajibuAI — Ask. Understand. Remember.**
