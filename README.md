# Music Live

### This project uses MetricStyle for Android 17 and BigTextStyle on Android 16 to display Live Update android 17 (API 37) with music playback management

![Android 16+](https://img.shields.io/badge/Android-16%2B-3DDC84?logo=android&logoColor=white)
![Min SDK](https://img.shields.io/badge/minSdk-36-blue)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)



## Visual project view 
<p align="center">
  <img src="screenshots/screen1.png" width="250" />
  <img src="screenshots/screen2.png" width="250" />
</p>


## Available versions of android:
| Android version | API | Support Status |
| :--- | :---: | :--- |
| **Android 17** | 37 | Full support (`MetricStyle` and chip Live Update) |
| **Android 16** | 36 | Basic work (`BigTextStyle`) |
| **Android 15 and below** | ≤ 35 | Not supported |


## Android Restrictions

### The official Live Update chip and the card in the curtain are the same
Notification of the advance. Public APIs do not allow you to hide the card from the curtain,
leaving only the chip. The system itself decides whether to show the chip, and it can
depend on the manufacturer of the device.
