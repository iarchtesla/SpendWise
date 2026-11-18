# SpendWise

SpendWise is a privacy-first, local-first Android financial tracker built with modern Android architecture. It reads bank SMS and payment notifications on-device, parses transactions, and keeps everything on your phone — nothing ever leaves it.

## ✨ Key Features

- **Automated Transaction Ingestion** — Parses bank SMS and app notifications (e.g. InstaPay) securely on-device. No SMS is uploaded, logged, or transmitted.
- **Bilingual Support** — Full English and Arabic support, with automatic system-language detection and a manual language switcher in Settings (via the AndroidX in-app language API).
- **RTL Layouts** — Complete Right-to-Left alignment for Arabic locales.
- **Dynamic Theming** — Automatic Material 3 Light and Dark mode driven by system settings.
- **Smart Deduplication** — Prevents duplicate records received via SMS and notifications using transaction references and merchant tokenization.

## 🛠 Tech Stack

- **Kotlin** — 100% Kotlin codebase
- **Jetpack Compose** — Modern declarative UI
- **Room Database** — Local persistence (with tested migrations)
- **Material 3** — Design system with dynamic Light/Dark color schemes
- **AndroidX Locales API** — In-app language switching (`AppCompatDelegate.setApplicationLocales`)

## 🔒 Privacy

SpendWise declares **no `INTERNET` permission**. All parsing, categorization, and storage happen locally. SMS access is used solely to detect bank transaction messages; you can revoke it at any time in system settings.

## 🚀 Quick Build

Requirements: JDK 21 and the Android SDK (set `sdk.dir` in `local.properties` or export `ANDROID_HOME`).

```bash
# Build the debug APK
./gradlew clean assembleDebug

# Run the unit test suite (parser, deduplication, currency utils)
./gradlew testDebugUnitTest
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

## 📦 Project Structure

```
app/src/main/java/com/islam/spendwise/
├── core/        # Currency, date and text utilities
├── data/local/  # Room database, DAOs, entities, migrations, seed data
├── parser/      # Regex transaction parser, deduplication engine, bank rules
├── service/     # SMS broadcast receiver, notification listener, ingestor
└── ui/          # Compose screens (dashboard, list, entry, reports, budgets, rules, settings)
```
