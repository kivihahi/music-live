package dev.wafla.musiclive.data

import dev.wafla.musiclive.R

data class UpdateEntry(
    val version: String,
    val title: String,
    val changes: List<String>,
    val imageRes: Int,
)

object JournalRepository {
    val updates = listOf(
        UpdateEntry(
            version = "1.2",
            title = "Релиз 1.2",
            changes = listOf(
                "Добавлены картинки в журнал обновлений",
                "Добавлены нормальные кнопки на главную чтобы интерфейс был понятнее",
                "Улучшены так же и функции показа Live уведомления",
            ),
            imageRes = R.drawable.journal_1_2,
        ),
        UpdateEntry(
            version = "1.1",
            title = "Обновление иконки",
            changes = listOf(
                "Новая иконка приложения",
                "Фон иконки в тёмном акценте Monet",
                "Новая иконка используется в приложении, уведомлении и чипе",
                "Цвета иконки подстраиваются под тему системы",
                "Иконка на чипе увеличен, иконка приложения уменьшена",
            ),
            imageRes = R.drawable.journal_1_1,
        ),
        UpdateEntry(
            version = "1.0",
            title = "Основные изменения",
            changes = listOf(
                "Live Update чип в статус баре",
                "Управление воспроизведением из уведомления",
                "Диагностика promoted-статуса",
                "Прошло и осталось в развёрнутом уведомлении",
                "Короткие кнопки: Назад, Пауза, Вперёд",
                "Две вкладки: Главная и Журнал",
                "Динамические цвета системы",
            ),
            imageRes = R.drawable.journal_1_0,
        ),
    )
}
