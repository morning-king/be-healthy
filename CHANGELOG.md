# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-02-13

### Added
- **Initial Release**: First version of BeHealthy application.
- **Fitness Plan**: Support for creating weekly/monthly fitness plans with custom goals.
- **Task Generation**: Automatic generation of daily tasks based on active plans.
- **Mood Tracking**: Feature to record daily mood with voice notes and text.
- **Statistics**: Visual charts for steps, calories, and exercise duration using Vico library.
- **Health Connect**: Integration with Android Health Connect for syncing step counts and calories.
- **Calendar**: Interactive calendar view with holiday support and daily task status.
- **Theming**: Multiple app themes (Wall-E, Doraemon, Nature, etc.) with dynamic switching.
- **Localization**: Support for Simplified Chinese.

### Technical
- Implemented Clean Architecture with MVVM pattern.
- Local data persistence using Room Database.
- UI built entirely with Jetpack Compose.
- Dependency injection with Hilt.
