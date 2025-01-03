# Persona Face Detection

Persona is an Android application for face detection and management, built with modern Android architecture components and Jetpack Compose.


| Full Access | Limited Access | Access Denied |
|:-----------:|:-------------:|:--------------:|
| <video src="https://github.com/user-attachments/assets/274ec590-fab2-4bd5-874b-35feb5fec66e" width="250"/> | <video src="https://github.com/user-attachments/assets/f718188c-a513-466c-9be2-c99fa3365ffa" width="250"/> | <video src="https://github.com/user-attachments/assets/ee2a3418-43ed-49ce-a849-96028876c332" width="250"/> |



Download APK from [Releases](https://github.com/vipulyaara/Persona/releases/tag/v0.0.1)

## Project Structure

The project follows a modular architecture with clear separation of concerns:

- **app** contains app launcher activity and application-level configuration
- **data** contains data models and repositories for low-level data manipulation
- **domain** is the gateway to data layer and handles business logic
- **ui/components** contains UI components commonly used by feature modules
- **ui/theme** contains theming and design-system
- **feature/face-detection** is feature module for face detection screen

## Key Features

- Image Processing
  - Adaptive image resizing that maintains detection accuracy while reducing memory footprint
  - Paginated batch processing of gallery images with memory-conscious loading
  - Preservation of aspect ratios for precise face detection overlays

- Face Detection
  - Integration with MediaPipe's face detection models
  - Real-time coordinate transformation system for accurate overlay positioning (bounding box and names)
  - Support for both CPU and GPU acceleration with automatic hardware optimization

- Performance Optimizations
  - Intelligent memory management with automatic bitmap recycling
  - Lazy loading of images in batches to prevent OOM errors
  - Software rendering fallback for maximum device compatibility
  - Automatic cleanup of ML resources and caching mechanisms to prevent memory leaks
 
- Face recognition to identify the same face in different photos can be implemented (probably using MediaPipe's Face Landmarker). Let me know if that should be implemented.

## Architecture

- MVVM with clean architecture
  - Jetpack compose for UI
  - ViewModels for lifecycle management
  - Use-cases for handling business logic

- Decoupled Processing Pipeline
  - Separate concerns for image loading, processing, face detection, and UI
  - Clean abstraction boundaries that allow for easy testing and modification


# Photo Permission System

This system handles photo access permissions across different Android versions, with special support for Android 14's partial photo access feature.

## Permission Flow

### Android 12L and Below
- Requires `READ_EXTERNAL_STORAGE` permission
- Once granted, app has access to all photos

### Android 13
- Uses `READ_MEDIA_IMAGES` permission
- Granular media type permission
- Full access to all photos when granted

### Android 14 and Above
- Primary permission: `READ_MEDIA_IMAGES`
- New partial access permission: `READ_MEDIA_VISUAL_USER_SELECTED`
- Users can:
  1. Grant full access to all photos
  2. Select specific photos (partial access) and change selected photos from UI within the app
  3. Deny access completely

## Important Notes
- Permission state persists across app restarts
- Users can modify permissions through system settings anytime
- Application backup is turned off in case we want to test different permission workflows by clearing data or uninstalling the app


## How to Run

### Prerequisites
- Android Studio Arctic Fox (2023.1.1) or newer
- JDK 17 or higher
- Android SDK with minimum API level 24 (Android 7.0)
- Android device or emulator running Android 7.0 or higher

### Setup Instructions
1. Clone the repository:
   ```bash
   git clone https://github.com/vipulyaara/Persona.git
   cd Persona
   ```

### Build and Run
1. Connect your Android device or start an emulator
2. Click the "Run" button or press Shift + F10
3. Wait for the app to build and install on your device

### Running Tests
- Run unit tests: `./gradlew test`

### Troubleshooting
- If you encounter Gradle sync issues:
  1. File -> Invalidate Caches / Restart
  2. Clean project (Build -> Clean Project)
  3. Rebuild project (Build -> Rebuild Project)
- Ensure you have the latest Android Studio updates
- Check that all SDK components are installed via SDK Manager


