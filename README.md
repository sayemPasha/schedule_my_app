![Profile Card](github_profile_card.png)

# Schedule My App

Schedule automatic launches of other installed applications at specified times. Built with Jetpack Compose and following modern Android development practices.

## Features

- 📱 Schedule app launches at specific times
- 📅 View and manage scheduled launches
- 🔍 Search and select from installed applications
- ⏰ Custom time picker for scheduling
- 📊 Track execution status of scheduled launches
- ❌ Cancel scheduled launches
- 🎨 Material Design 3 UI with dynamic theming

## Architecture & Technical Details

### Tech Stack

- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt
- **Background Processing**: WorkManager
- **Local Storage**: Room Database
- **State Management**: Kotlin Flow & StateFlow
- **Navigation**: Jetpack Navigation Compose

### Project Structure

```
app/src/main/java/com/sayem/main/
├── data/                  # Data layer with repositories and data sources
│   ├── local/            # Local database implementation
│   └── di/               # Dependency injection modules
├── di/                   # App-level dependency injection
├── ui/                   # UI layer with screens and viewmodels
│   ├── metaitem/        # Meta item management
│   ├── scheduler/       # Scheduling functionality
│   │   ├── create/     # Create new schedules
│   │   ├── details/    # Schedule details
│   │   └── list/       # List of schedules
│   ├── shared/         # Shared UI components
│   └── theme/          # App theming
├── utils/               # Utility classes
└── worker/             # Background work handling
```

## Requirements

- Android Studio Arctic Fox or newer
- Minimum SDK: 21 (Android 5.0)
- Target SDK: 35
- Java 17

## Dependencies

### Core Dependencies
- AndroidX Core KTX
- Kotlin Coroutines
- Lifecycle Runtime KTX
- WorkManager KTX

### UI & Compose
- Compose BOM
- Compose UI
- Compose Material 3
- Compose Navigation
- Accompanist Drawable Painter

### Dependency Injection
- Hilt Android
- Hilt Navigation Compose
- Hilt Work

### Local Storage
- Room Runtime
- Room KTX

### Testing
- JUnit
- Coroutines Test
- AndroidX Test
- Hilt Testing
- Compose Testing

## Setup Instructions

1. Clone the repository
2. Open the project in Android Studio
3. Sync project with Gradle files
4. Run the app on an emulator or physical device

## Build Configuration

The app uses Gradle with Kotlin DSL (`build.gradle.kts`) for build configuration. Key configurations include:

- Compose enabled
- Java 17 compatibility
- Room schema location configuration
- Proguard configuration for release builds
