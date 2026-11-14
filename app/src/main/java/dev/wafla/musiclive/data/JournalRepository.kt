package dev.wafla.musiclive.data

data class UpdateEntry(
    val version: String,
    val title: String,
    val changes: List<String>,
)

object JournalRepository {
    val updates = listOf(
        UpdateEntry(
            version = "1.0",
            title = "Основные изменения",
            changes = listOf(
                "Live Update-чип в статус-баре",
                "Управление воспроизведением из уведомления",
                "Диагностика promoted-статуса",
                "Прошло и осталось в развёрнутом уведомлении",
                "Короткие кнопки: Назад, Пауза, Вперёд",
                "Время убрано из статус-бара",
                "Две вкладки: Главная и Журнал",
                "Стандартная нижняя навигация Material 3",
                "Динамические цвета системы",
                "Фон иконки в среднем акценте темы",
                "Новая иконка в уведомлении и интерфейсе",
                "История треков убрана",
            ),
        ),
    )
}
