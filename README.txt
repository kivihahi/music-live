# Music Live

### This project uses MetricStyle for Android 17 and BigTextStyle on Android 16 to display Live Update android 17 (API 37) with music playback management

![Android](https://shields.io)
![Kotlin](https://shields.io)


## Visual project view 
<p align="center">
  <img src="screenshots/screen1.png" width="250" />
  <img src="screenshots/screen2.png" width="250" />
</p>


## Available versions of android:
| Версия Android | API | Статус поддержки |
| :--- | :---: | :--- |
| **Android 17** | 37 | Полная поддержка (`MetricStyle` и чип Live Update) |
| **Android 16** | 36 | Базовая работа (`BigTextStyle`) |
| **Android 15 и ниже** | ≤ 35 | Не поддерживается |


## Android Restrictions

### The official Live Update chip and the card in the curtain are the same
Notification of the advance. Public APIs do not allow you to hide the card from the curtain,
leaving only the chip. The system itself decides whether to show the chip, and it can
depend on the manufacturer of the device.
