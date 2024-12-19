# Persona Face Detection

Persona is an Android application for face detection and management, built with modern Android architecture components and Jetpack Compose.

## Project Structure

The project follows a modular architecture with clear separation of concerns:

- **app** contains app launcher activity and application-level configuration
- **data** contains data models and repositories for low-level data manipulation
- **domain** is gateway to data layer and handles business logic
- **ui/components** contains UI components commonly used by feature modules
- **ui/theme** contains theming and design-system
- **feature/face-detection** is feature module for face detection screen

## Key Features

- Image Processing
  - Adaptive image resizing that maintains detection accuracy while reducing memory footprint
  - Efficient batch processing of gallery images with memory-conscious loading
  - Preservation of aspect ratios for precise face detection overlays

- Face Detection
  - Integration with MediaPipe's face detection models
  - Real-time coordinate transformation system for accurate overlay positioning
  - Support for both CPU and GPU acceleration with automatic hardware optimization

- Performance Optimizations
  - Intelligent memory management with automatic bitmap recycling
  - Lazy loading of images in batches to prevent OOM errors
  - Software rendering fallback for maximum device compatibility

## Architecture

- MVVM with clean architecture
  - ViewModels for lifecycle management
  - Use-cases for handling business logic

- Decoupled Processing Pipeline
  - Separate concerns for image loading, processing, and face detection
  - Efficient coordination between components through coroutine flows
  - Clean abstraction boundaries that allow for easy testing and modification

- Resource Management
  - Automatic cleanup of ML resources to prevent memory leaks
  - Smart caching system that balances memory usage with performance
  - Graceful handling of system-induced resource reclamation

## Future Enhancements

- Face recognition to identify same face in different photos

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
- Partial access feature only works on Android 14 and above
- Older versions fall back to traditional permission model
- Permission state persists across app restarts
- Users can modify permissions through system settings anytime
- Application backup is turned off in case we want to test different permission workflows by clearing data or uninstalling the app

| Full Access | Limited Access | Face Detection |
|:-----------:|:-------------:|:--------------:|
| <!-- Video 1 --> | <!-- Video 2 --> | <!-- Video 3 --> |
