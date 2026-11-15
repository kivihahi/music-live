# Music Live

### This project uses MetricStyle for Android 17 and BigTextStyle on Android 16 to display Live Update android 17 (API 37) with music playback management

![Android 16+](https://img.shields.io/badge/Android-16%2B-3DDC84?logo=android&logoColor=white)
![Min SDK](https://img.shields.io/badge/minSdk-36-blue)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)


## Visual project view

| | | |
|:---:|:---:|:---:|
| ![Status bar](screenshots/banner1.png) | ![Controls](screenshots/banner2.png) | ![Compatibility](screenshots/banner3.png) |



## Available versions of android:
| Android version | API | Support Status |
| :--- | :---: | :--- |
| **Android 17** | 37 | Full support (`MetricStyle` and chip Live Update) |
| **Android 16** | 36 | Basic work (`BigTextStyle`) |
| **Android 15 and below** | ≤ 35 | Not supported |

## Assembly

### 1. Clone the repository and build the debug version
```bash
git clone https://github.com/kivihahi/music-live.git
cd music-live
./gradlew :app:assembleDebug
```

### 2. Run project verification (Lint)
```bash
./gradlew :app:lintDebug
```

### 3. Output APK location
After a successful build, you can find the generated APK file at:
```bash
app/build/outputs/apk/debug/app-debug.apk
```

## Android Restrictions

### The official Live Update chip and the card in the curtain are the same
Notification of the advance. Public APIs do not allow you to hide the card from the curtain,
leaving only the chip. The system itself decides whether to show the chip, and it can
depend on the manufacturer of the device.
