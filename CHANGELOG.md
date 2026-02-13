# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - 2026-02-13

### Added
- **Global Font Color Mode**: New setting in Profile -> Theme to force Light, Dark, or Auto font colors across all themes.
- **Zen Theme Rotation**: Dynamic "Zen" (禅) character rotation with customizable speed, direction, and toggle controls.
- **Tech Theme Customization**: Enhanced Cyberpunk matrix background with 3 intensity levels (Minimal, Standard, Vibrant).
- **Back to Today**: Added "Back to Today" floating action button in Fitness Plan and Task Calendar screens.

### Fixed
- **WCAG Compliance**: Optimized "Snooker" and "Wall-E" themes to meet WCAG 2.1 AA contrast standards.
- **Crash Fixes**: Resolved crashes in Statistics screen (missing content) and "Back to Today" navigation.
- **Compilation**: Resolved conflicting function overloads in Theme and Statistics modules.
- **Persistence**: Fixed issue where theme settings were not correctly persisting across sessions.

## [1.1.0] - 2026-02-13

### Added
- **Unified Trend Chart**: New interactive chart visualizing steps, calories, and duration in a single view with responsive design and tooltips.
- **Content Expansion**: Expanded "Daily Quote" and "Daily Poem" database by 7x (100+ items) with new categories and authors.
- **Filter Optimization**: Added "Last 2 Weeks" filter option and optimized filter chip UI for better space utilization.

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
