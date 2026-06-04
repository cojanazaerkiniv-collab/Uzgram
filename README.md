# UzGram Android App

Native Android messenger built with **Kotlin 2.0 + Jetpack Compose + MVVM + Clean Architecture**.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Networking | Retrofit + OkHttp |
| WebSocket | Socket.IO client |
| Database | Room |
| Preferences | DataStore |
| Images | Coil |
| Crypto | BouncyCastle + AES-256-IGE |
| Calls | WebRTC (stream-webrtc-android) |
| Media | ExoPlayer, CameraX |
| Async | Kotlin Coroutines + Flow |
| Animations | Lottie |

## Build Requirements

- **Android Studio Ladybug** (2024.2.1) or newer
- **JDK 17**
- **Android SDK** 35 (Android 15)
- **Min SDK** 26 (Android 8.0)

## Setup

1. Clone the repo and open the `android/` folder in Android Studio.
2. Set your backend URL in `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"https://your-server/api/v1/\"")
   buildConfigField("String", "WS_URL",   "\"wss://your-ws-server\"")
   ```
3. Add your `google-services.json` (from Firebase Console) to `app/`.
4. Build → Run.

## Project Structure

```
app/src/main/java/com/uzgram/messenger/
├── di/                    Hilt dependency injection modules
├── domain/
│   ├── model/             Domain models (User, Chat, Message)
│   └── repository/        Repository interfaces
├── data/
│   ├── local/
│   │   ├── dao/           Room DAOs
│   │   ├── db/            Room database + converters
│   │   ├── entity/        Room entities
│   │   └── prefs/         DataStore preferences
│   ├── remote/
│   │   ├── api/           Retrofit API service
│   │   ├── dto/           Request / response DTOs
│   │   ├── interceptor/   OkHttp interceptors
│   │   └── websocket/     Socket.IO manager + events
│   └── repository/        Repository implementations + mappers
├── features/
│   ├── auth/              Login, Register, AuthViewModel
│   ├── calls/             Voice/video calls (WebRTC)
│   ├── chat/              Chat list screen + ViewModel
│   ├── messaging/         Messaging screen + ViewModel
│   ├── miniapps/          WebView mini-app container
│   └── notifications/     FCM service + call receiver
├── navigation/            NavGraph + Screen routes
├── ui/
│   ├── main/              MainScreen + bottom navigation
│   └── theme/             Colors, typography, shapes
└── utils/                 Extensions, Result, MediaUtils, NetworkMonitor
```

## Features

- End-to-end encrypted messages (AES-256-IGE, UzProto)
- Real-time WebSocket messaging
- Voice and video calls (WebRTC)
- Photo/video/file sharing
- Push notifications (FCM)
- Message reactions, replies, pinning
- Group chats and channels
- Stories
- Mini-app platform (WebView)
- Biometric app lock
- Dark/light theme
- Uzbek language support

## Brand Colors

| Name | Hex |
|------|-----|
| Primary | `#2AABEE` |
| Deep | `#229ED9` |
| Dark Background | `#0E0E0E` |
| Dark Surface | `#1C1C1C` |
