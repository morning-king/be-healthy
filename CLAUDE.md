# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**BeHealthy** is a native Android fitness tracking application (Kotlin + Jetpack Compose) designed for personal health monitoring. It's a local-first app with no backend - all data is stored locally using Room database.

**Target Device**: OPPO Find X6 Pro (Android 13+)
**Language**: Kotlin
**UI Framework**: Jetpack Compose with Material3
**Design Style**: Ant Design Mobile (British refined style - deep blue, dark red, off-white)

## Development Commands

### Environment Setup
```bash
# Run the automated setup script (installs JDK 17, Android Studio, configures env vars)
./scripts/setup_dev_env.sh

# After running the script, reload your shell
source ~/.zshrc  # or ~/.bash_profile
```

### Build & Run
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

### Gradle Tasks
```bash
# Clean build
./gradlew clean

# View available tasks
./gradlew tasks
```

## Architecture

The project follows **Clean Architecture** with **MVVM** pattern:

```
com.behealthy.app
├── core/
│   ├── database/          # Room DB, DAOs, Entities
│   ├── designsystem/      # Theme, colors, common components (Ant Design style)
│   └── util/              # Extension functions, helpers
├── feature/               # Feature modules (UI + ViewModels)
│   ├── plan/              # Fitness plan management (List, Create, Edit)
│   ├── task/              # Daily task execution (Calendar, Detail, Check-in)
│   ├── stats/             # Statistics and analytics
│   └── mood/              # Mood tracking
├── di/                    # Hilt dependency injection modules
└── BeHealthyApp.kt        # Application entry point (Hilt + WorkManager setup)
```

### Key Technologies
- **DI**: Hilt/Dagger (2.50)
- **Database**: Room (2.6.1) - SQLite with type-safe queries
- **Async**: Coroutines + Flow
- **Background Jobs**: WorkManager (periodic sync every 5 min, daily task generation at 00:00)
- **Navigation**: Compose Navigation (single-activity architecture)
- **Image Loading**: Coil (2.5.0)
- **Charts**: Vico (1.13.0) - Compose-friendly charting library

### Data Layer

**Entities** (Room tables):
- `FitnessPlan` - Workout plans with duration (MONTH/WEEK/DAY), goals, work/rest day configs
- `FitnessTask` - Daily tasks generated from plans (diet records, exercise logs, completion status)
- `MoodRecord` - Daily mood tracking
- `SyncData` - Cached step counter data (steps, calories, distance)

**Repositories**: Abstract data access, expose Flow/StateFlow for reactive UI

### UI Layer

- **Screens**: Jetpack Compose @Composable functions
- **ViewModels**: Hold UI state, expose flows from repositories
- **Navigation**: Single Activity with Compose Navigation
- **Theme**: Material3 with custom Ant Design-inspired colors (British refined style)

### Background Work

- **Step Counter Sync**: PeriodicWorkRequest every 5 minutes using SensorManager.TYPE_STEP_COUNTER
- **Daily Task Generation**: WorkManager job at 00:00 to create FitnessTasks from ACTIVE FitnessPlans
- **Notifications**: Scheduled at 09:15, 12:15, 18:30, 23:10 for task reminders

## Product Requirements Context

This is a personal health app for a pre-diabetic user monitoring diet and exercise for 2 months. Key features:

1. **Fitness Plans** - Create plans with work/rest day configurations (diet + exercise targets)
2. **Daily Tasks** - Auto-generated from plans; track meals (photos + calories) and exercise (type, duration, steps, screenshots)
3. **Calendar View** - Visual task completion status (red=incomplete, green=complete, orange=partial)
4. **Step Sync** - Auto-sync from phone's pedometer every 5 min
5. **Mood Tracking** - Daily mood with calendar and statistics
6. **Daily Wisdom** - Tao Te Ching quotes (3 per day)
7. **Persistent Notification** - Shows daily progress (similar to iOS Dynamic Island)

## Design Considerations

- **Local-first**: All data stored in Room, no network calls
- **Offline capable**: No server dependency
- **Battery efficient**: WorkManager for background jobs, exponential backoff for retries
- **Chinese language**: UI and content in Chinese (Simplified)
- **Emoji support**: Fields support emoji input for goals and notes

## File Locations

- **Manifest**: `app/src/main/AndroidManifest.xml`
- **Main Activity**: `app/src/main/java/com/behealthy/app/MainActivity.kt`
- **Database**: `app/src/main/java/com/behealthy/app/core/database/BeHealthyDatabase.kt`
- **Build config**: `app/build.gradle.kts` (dependencies), `build.gradle.kts` (project-level)
- **Requirements**: `requirements/v1.0.md` (PRD), `docs/technical_design.md` (architecture spec)
