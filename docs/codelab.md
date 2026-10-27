# Build MajibuAI: From Cloud Chat to Hybrid AI

MajibuAI is an Android learning companion built with Kotlin, Jetpack Compose, and Kotlin Flow. In this codelab, you’ll start with the chat UI and progressively add cloud AI providers, streaming, on-device inference, and hybrid routing.

At each step, you’ll see what to build, why the design is structured that way, and how to tell when the step works.

## What you’ll build

By the end, you’ll be able to:

- Send chat requests to Firebase AI Logic and OpenRouter.
- Switch providers without changing the chat screen’s request flow.
- Stream answers into the chat UI.
- Download and run a Gemma model on-device with LiteRT-LM.
- Route to local AI when cloud providers are unavailable.
- Handle provider errors consistently.
- Compare privacy, latency, and cost trade-offs between cloud and local inference.

## Before you start

Complete the instructions in [`setup.md`](setup.md), then confirm that the starter app runs.

You’ll need Firebase configuration and an OpenRouter API key for the cloud-provider checkpoints. The on-device checkpoint downloads its model the first time you choose **On-device**.

## Work through the checkpoints

The repository has solution branches for each stage. Create your own branch from the starter so you can keep your work as you go:

```bash
git switch -c droidcon-codelab 00-starter
```

Treat the checkpoint branches as references. After trying a step, compare your work with the matching branch. For example:

```bash
git diff 01-cloud-ai -- app/src/main/java/com/valentinerutto/majibuai
```

This lets you inspect the solution without replacing your working branch.

| Checkpoint branch | Focus |
|---|---|
| `00-starter` | Starting app and chat UI |
| `01-cloud-ai` | Firebase AI Logic |
| `02-multi-provider` | Provider abstraction and OpenRouter |
| `03-streaming` | Streaming responses |
| `04-on-device-ai` | LiteRT-LM and local model management |
| `o5-hybrid-ai` | Cloud and on-device routing |
| `main` | Completed reference app |

## 1. Start with the chat flow

Explore the starter app before adding AI. Follow the path from the Compose screen to the ViewModel and identify where the question and answer appear in UI state.

**Look at:**

- `ui/ask/AskScreen.kt`
- `ui/MajibuViewModel.kt`

**Try:** Run the app, submit a question, and find where the ViewModel represents the loading, conversation, and error states.

**Why this structure:** The screen renders state and reports user actions. Keeping request logic in the ViewModel and data layer makes it possible to replace an AI provider without rewriting the UI.

**Checkpoint:** The starter app launches and you can trace a question from the input field to the displayed answer state.

## 2. Add Firebase AI Logic

Set up Firebase AI Logic and App Check using [`setup.md`](setup.md). Add a Firebase engine that creates the model, applies MajibuAI’s system instruction, and sends the conversation.

**Look at:**

- `data/ai/firebase/FirebaseGeminiEngine.kt`
- `data/ai/models/FirebaseChatModels.kt`
- `MajibuApp.kt`
- `di/appModule.kt`

**Try:** Ask Gemini a question and then ask a follow-up that depends on the first answer.

**Why use an engine:** Firebase-specific model creation and SDK calls belong behind an engine boundary. The repository can work with app-level chat messages instead of Firebase SDK types, and the Compose UI stays independent of Firebase.

**Checkpoint:** Firebase returns answers, follow-up messages include the earlier conversation, and configuration or request failures reach the UI as errors.

## 3. Add provider switching and OpenRouter

Add a shared provider choice and connect OpenRouter through Retrofit. Map app chat messages to the provider’s request format and map the response back into the app’s chat response model.

**Look at:**

- `util/AiProvider.kt`
- `data/ai/openrouter/`
- `network/RetrofitClient.kt`
- `data/ai/AiRepository.kt`
- `ui/ask/AskScreen.kt`

**Try:** Switch between Firebase and OpenRouter and ask the same question from the same chat screen.

**Why use a repository router:** Provider selection belongs in the repository layer. Each engine handles its own network or SDK details, while the ViewModel can keep using the same chat request and result types.

**Checkpoint:** Both cloud providers can answer through the same chat UI, and changing the provider does not require a separate screen or ViewModel flow.

## 4. Stream answers into the chat

Change each cloud engine to emit text chunks as they arrive. Collect the chunks in the ViewModel and append them to the current assistant message. Add a stop action that cancels the active generation.

**Look at:**

- `data/ai/firebase/FirebaseGeminiEngine.kt`
- `data/ai/openrouter/OpenRouterEngine.kt`
- `ui/MajibuViewModel.kt`
- `ui/ask/AskScreen.kt`

**Try:** Ask a question and observe the answer appear incrementally. Stop generation while a response is in progress.

**Why use Flow:** Streaming provides visible progress while the provider is generating. The ViewModel can update one assistant message as chunks arrive, and coroutine cancellation provides a consistent way to stop a request.

**Checkpoint:** Firebase and OpenRouter stream answers, and stopping a response leaves the UI usable.

## 5. Add on-device inference

Add a model manager to download the LiteRT-LM model and an engine to initialize it and generate responses. Show download and initialization state in the UI.

**Look at:**

- `data/ai/ondevice/OnDeviceModelManager.kt`
- `data/ai/ondevice/LiteRTLmEngine.kt`
- `data/ai/models/OnDeviceModelStatus.kt`
- `ui/MajibuViewModel.kt`
- `di/appModule.kt`

**Try:** Choose **On-device**, download the model, wait until it is ready, then ask a question. Once the model is ready, try the app with the network disabled.

**Why separate downloading from inference:** Downloading is a network and storage operation; generation is a local model operation. Keeping them separate makes progress, cancellation, initialization failures, and retry behavior easier to manage.

**Checkpoint:** The app only downloads the model after the user requests it, shows its state, and can generate a response locally after initialization.

## 6. Add hybrid routing

Add a Hybrid provider path in `AiRepository`. The current implementation tries Firebase and then OpenRouter while online. If neither produces an answer and the on-device model is ready, it can use local inference. If the app is offline, it skips cloud providers and can use the ready local model directly.

**Try:**

1. With a connection and the local model ready, ask a question using Hybrid.
2. Disable the network and ask another question.
3. Restore the network and try again.
4. Start a response, then interrupt the connection and observe the retry behavior.

**Why the fallback depends on whether text has started:** Before any answer is shown, the app can switch providers automatically. Once streamed text is visible, switching providers could produce a second or inconsistent answer. In that case, the app keeps the partial response and lets the user decide whether to retry.

**Checkpoint:** Hybrid mode uses local inference when cloud providers cannot answer before streaming begins, and offers a user-controlled retry if an answer has already started.

## 7. Make errors clear and consistent

Use the shared `AiError` model to turn provider failures into app-level error types. Keep provider-specific parsing inside the provider engine and map errors into messages that make sense in the UI.

**Look at:**

- `data/ai/models/AiError.kt`
- `data/ai/openrouter/OpenRouteraiError.kt`
- `data/ai/AiRepository.kt`
- `ui/ask/AskScreen.kt`

**Try:** Test a missing OpenRouter key, a Firebase quota failure, and an unavailable on-device model.

**Why use typed errors:** The UI should respond to the kind of failure rather than needing to understand provider exception classes. This also lets the app give useful guidance for network, quota, App Check, and model readiness problems.

**Checkpoint:** Errors appear as actionable messages, cancellation is not shown as a failure, and one provider’s error format does not leak into the other engines.

## 8. Compare the provider trade-offs

| Choice | Where inference happens | What to consider |
|---|---|---|
| Firebase or OpenRouter | Cloud | Requires connectivity; request history is sent to the selected provider; latency, quota, and cost depend on provider and usage. |
| On-device | User’s device | Inference can work offline; model download uses storage and bandwidth; generation uses device resources. |
| Hybrid | Cloud when available, local fallback | Provides more continuity, while requiring explicit rules for when a request may switch providers. |

Discuss which provider you would choose for a sensitive question, a quick response on a slow connection, or an offline session—and why.

## Final checklist

- [ ] The app launches from your codelab branch.
- [ ] Firebase and OpenRouter can answer from the same chat screen.
- [ ] Cloud answers stream incrementally.
- [ ] You can stop generation.
- [ ] The on-device model downloads only after the user requests it.
- [ ] On-device inference works after the model is ready.
- [ ] Hybrid mode uses the local model when cloud requests fail before streaming.
- [ ] An interrupted streamed answer offers a user-controlled retry.
- [ ] Errors are shown as useful, provider-independent messages.
- [ ] You can explain the privacy, latency, and cost trade-offs.

## Explore the completed app

Switch to `main` when you want to compare your work with the completed reference:

```bash
git switch main
```

Return to your working branch whenever you want to continue the codelab.
