# PackPilot

[![Code Coverage](https://qlty.sh/badges/58bb7a19-645d-4ca2-ace9-0729ba2275cc/coverage.svg)](https://qlty.sh/gh/Luca-Engel/projects/PackPilot)
[![Maintainability](https://qlty.sh/badges/58bb7a19-645d-4ca2-ace9-0729ba2275cc/maintainability.svg)](https://qlty.sh/gh/Luca-Engel/projects/PackPilot)

A cross-platform packing list app built with **Kotlin Multiplatform** and **Compose Multiplatform**. Plan what to pack for your trips, manage essential items, and create reusable activity templates — all from a single shared codebase targeting Android and iOS.

## Features

- **Trip Management** — Create trips with a name, date range, activity type, and laundry interval. View planned and past trips from the home screen.
- **Smart Quantity Calculation** — Item quantities adjust automatically based on trip duration and laundry frequency.
- **Essential Items** — Maintain a library of items that are added to every trip automatically.
- **Trip Types** — Define activity-based templates (e.g. Beach, Hiking, Business) with their own item lists. Select a type when creating a trip to pre-populate it.
- **Item Categories** — Organize items into Clothing, Toiletries, Electronics, Documents, Food, Equipment, and Other.
- **Pack Tracking** — Mark items as packed or unpacked to track your progress.
- **Undo / Redo** — Full edit history support across all screens.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.2 |
| UI | Compose Multiplatform 1.7, Material 3 |
| Navigation | Jetpack Navigation Compose |
| State | StateFlow, MVVM |
| Persistence | AndroidX DataStore with Kotlinx Serialization (JSON) |
| Async | Kotlinx Coroutines |
| Date/Time | Kotlinx DateTime |
| Testing | Kotlin Test, Turbine, Espresso |
| Coverage | Kover (60 % minimum) |

## Project Structure

```
PackPilot/
├── composeApp/                     # Shared KMP module
│   └── src/
│       ├── commonMain/             # Shared code
│       │   ├── model/              # Data models (Trip, PackingItem, …)
│       │   ├── repository/         # DataStore persistence layer
│       │   ├── viewmodel/          # PackingViewModel — app state & logic
│       │   └── ui/
│       │       ├── screens/        # Home, Create, Details, Essentials, Types
│       │       ├── components/     # Reusable UI components
│       │       └── navigation/     # Route definitions
│       ├── androidMain/            # Android-specific implementations
│       ├── iosMain/                # iOS-specific implementations
│       ├── commonTest/             # Unit tests
│       └── androidInstrumentedTest/# UI tests (Espresso)
├── androidApp/                     # Android application entry point
├── iosApp/                         # iOS application (Xcode project)
└── gradle/libs.versions.toml      # Version catalog
```

## Getting Started

### Prerequisites

- **JDK 17+**
- **Android Studio** (latest stable) with the Kotlin Multiplatform plugin
- For iOS: **Xcode 15+** and a macOS machine

### Build & Run

```bash
# Android
./gradlew :androidApp:installDebug

# Run common unit tests
./gradlew :composeApp:allTests

# iOS (from Xcode)
# Open iosApp/ in Xcode and run on a simulator or device.
```

## License

See the repository for license details.
