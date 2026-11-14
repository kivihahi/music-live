Music Live
==========

Приложение показывает текущую медиасессию в виде Live Update-уведомления
Android 17 (API 37) с управлением воспроизведением.

Сборка
------
./gradlew :app:assembleDebug
./gradlew :app:lintDebug

Готовый APK:
app/build/outputs/apk/debug/app-debug.apk

Структура проекта
-----------------
app/src/main/java/dev/wafla/musiclive/MainActivity.kt
    Точка входа приложения, запускает Compose-интерфейс и обновляет экран
    при возврате в приложение.

app/src/main/java/dev/wafla/musiclive/data/AppPrefs.kt
    Настройки приложения. Сейчас хранит только переключатель кнопок
    управления в уведомлении.

app/src/main/java/dev/wafla/musiclive/data/JournalRepository.kt
    Список обновлений приложения для вкладки "Журнал". Истории треков нет.

app/src/main/java/dev/wafla/musiclive/data/MediaAction.kt
    Перечисление действий: предыдущий трек, пауза/воспроизведение,
    следующий трек.

app/src/main/java/dev/wafla/musiclive/data/MusicRepository.kt
    Подключение к активной медиасессии через MediaSessionManager.
    Читает название, исполнителя, обложку, позицию, длительность и состояние
    воспроизведения.

app/src/main/java/dev/wafla/musiclive/data/NowPlaying.kt
    Модель текущего трека и функция форматирования длительности.

app/src/main/java/dev/wafla/musiclive/notification/LiveUpdateStatus.kt
    Состояние диагностики: запущен ли сервис, есть ли promotable-признаки,
    разрешены ли promoted-уведомления, получен ли фактический promoted-статус,
    последняя ошибка.

app/src/main/java/dev/wafla/musiclive/notification/NotificationFactory.kt
    Создаёт Live Update-уведомление: канал, метрики "Прошло" и "Осталось"
    на Android 17, кнопки управления и запрос promoted-статуса.
    В статус-баре у чипа нет времени, показывается только иконка.

app/src/main/java/dev/wafla/musiclive/service/LiveUpdateService.kt
    Foreground-сервис. Запускает таймер обновления уведомления,
    публикует и обновляет Live Update, останавливает уведомление.

app/src/main/java/dev/wafla/musiclive/service/MediaActionReceiver.kt
    Принимает нажатия кнопок уведомления и передаёт команды в медиасессию.

app/src/main/java/dev/wafla/musiclive/service/MusicListenerService.kt
    NotificationListenerService, который получает доступ к медиасессиям
    и сообщает об изменениях текущего трека.

app/src/main/java/dev/wafla/musiclive/ui/MusicLiveApp.kt
    Главный экран: текущий трек, разрешения, запуск и остановка Live Update,
    кнопки управления, диагностика promoted-статуса.
    Нижняя навигация: "Главная" и "Журнал".

app/src/main/java/dev/wafla/musiclive/ui/JournalScreen.kt
    Вкладка "Журнал": описание обновлений приложения.

app/src/main/java/dev/wafla/musiclive/ui/Theme.kt
    Тема Material 3 с динамическими цветами системы (Material You / Monet).

app/src/main/AndroidManifest.xml
    Разрешения, объявление Activity, foreground-сервиса и
    NotificationListenerService.

app/src/main/res/values/strings.xml
    Текстовые строки приложения.

app/src/main/res/values/themes.xml
    Базовая тема приложения.

app/src/main/res/drawable/
    Векторные иконки: транспорт, музыкальная нота, вкладки.
    Нота в уведомлении и интерфейсе использует форму новой иконки.

app/src/main/res/mipmap-anydpi-v26/
    Адаптивная иконка приложения. Фон берётся из среднего акцента темы
    (system_accent1_500), нота светлая (system_neutral1_0), есть
    monochrome-слой для тематических иконок.

app/build.gradle.kts
    Настройки Android-модуля, зависимости Compose и Material 3,
    версия приложения.

build.gradle.kts, settings.gradle.kts, gradle.properties
    Настройки Gradle-проекта.

Уведомление
-----------
Канал: music_live_update
Важность: LOW
ID уведомления: 1001
В развёрнутом виде: "Прошло" и "Осталось"
Кнопки: "Назад", "Пауза"/"Играть", "Вперёд"

Совместимость
-------------
Android 17 (API 37): полная поддержка, MetricStyle и promoted-уведомление.
Android 16 (API 36): приложение работает, но используется BigTextStyle;
запрос promoted-ongoing доступен только начиная с 36.1.
Android 15 и ниже: не поддерживается, minSdk 36.

Ограничения Android
-------------------
Официальный чип Live Update и карточка в шторке - одно и то же
promoted-уведомление. Публичные API не позволяют скрыть карточку из шторки,
оставив только чип. Система сама решает, показывать ли чип, и это может
зависеть от производителя устройства.

MetricStyle в Android 17 не показывает largeIcon, поэтому обложка
выводится только в интерфейсе приложения, но не в уведомлении.

Логи
----
adb logcat -s MusicLive.Service MusicLive.Listener MusicLive.Repository
