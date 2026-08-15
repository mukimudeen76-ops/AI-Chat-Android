# ThinkForge AI Pro 🤖

**ThinkForge AI** — एक Android AI Chat App जो आपके डिवाइस पर Locally चलता है।

| | |
|---|---|
| **Architecture** | Mixture of Experts (MoE) |
| **Total Parameters** | 671B |
| **Active per Token** | 45B |
| **Context Window** | 1,000,000 tokens |
| **Quantization** | Q4_K_M (~36GB) |
| **License** | MIT |

## ✨ Features

- **🚀 Fully Local** — No internet needed after model download
- **🔒 Private** — Everything runs on your device, zero telemetry
- **🧠 1M Context** — Million-token conversations & documents
- **💬 Smart Chat** — Reasoning, coding, math, analysis
- **🎨 Material You** — Modern Android design with light/dark theme
- **📱 Optimized** — ARM64 native code for Android
- **🔄 Ollama Support** — Can use Ollama backend as alternative
- **💾 History** — Save and export chat sessions

## 📱 Screenshots

*[Screenshots would be added here]*

## 🛠️ Tech Stack

- **UI:** Kotlin, Material 3, ViewPager2, RecyclerView
- **Engine:** C++17, JNI, llama.cpp compatible
- **Backend:** GGUF file format / Ollama API
- **Storage:** Room Database, JSON files
- **Build:** Gradle, CMake, NDK

## 📦 Download

Get the APK from the [Releases](https://github.com/thinkforge-ai/thinkforge-android/releases) page.

## 🔧 Build from Source

1. Clone the repo
2. Open in Android Studio
3. Sync Gradle
4. Build & Run

```bash
git clone https://github.com/thinkforge-ai/thinkforge-android.git
cd thinkforge-android
./gradlew assembleRelease
```

## 📥 Model Download

After installing, open the app and go to **Models** tab to download the GGUF model file (~36GB).

Or manually:
```bash
huggingface-cli download deepseek-ai/DeepSeek-V4-Pro-0813-GGUF \
  --include "deepseek-v4-pro-0813-Q4_K_M.gguf" \
  --local-dir ./ThinkForgeAI/models
```

## 📋 Requirements

| Component | Minimum | Recommended |
|---|---|---|
| **RAM** | 8 GB | 16 GB+ |
| **Storage** | 40 GB free | 80 GB+ |
| **GPU** | Adreno 7xx / Mali-G7x | Snapdragon 8 Gen 3+ |
| **Android** | API 26+ (Android 8.0) | API 34+ (Android 14) |

## ⚖️ License

MIT License - Copyright (c) 2026 ThinkForge AI

Based on the DeepSeek V4 Pro 0813 desktop application (MIT License).
The DeepSeek V4 Pro model weights are released under the DeepSeek Community License.

## 🙏 Credits

- [DeepSeek V4 Pro 0813](https://github.com/deepseek-v4-pro-0813/deepseek-v4-pro-0813) — Original desktop app
- [llama.cpp](https://github.com/ggerganov/llama.cpp) — GGUF inference engine
- [Hugging Face](https://huggingface.co/deepseek-ai/DeepSeek-V4-Pro-0813-GGUF) — Model weights