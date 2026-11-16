package dev.wafla.musiclive.data

import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.wafla.musiclive.R

data class UpdateEntry(
    val version: String,
    @StringRes val titleRes: Int,
    @ArrayRes val changesRes: Int,
    @DrawableRes val imageRes: Int,
)

object JournalRepository {
    val updates = listOf(
        UpdateEntry(
            version = "1.4 beta",
            titleRes = R.string.journal_1_4_title,
            changesRes = R.array.journal_1_4_changes,
            imageRes = R.drawable.journal_1_4,
        ),
        UpdateEntry(
            version = "1.3",
            titleRes = R.string.journal_1_3_title,
            changesRes = R.array.journal_1_3_changes,
            imageRes = R.drawable.journal_1_3,
        ),
        UpdateEntry(
            version = "1.2",
            titleRes = R.string.journal_1_2_title,
            changesRes = R.array.journal_1_2_changes,
            imageRes = R.drawable.journal_1_2,
        ),
        UpdateEntry(
            version = "1.1",
            titleRes = R.string.journal_1_1_title,
            changesRes = R.array.journal_1_1_changes,
            imageRes = R.drawable.journal_1_1,
        ),
        UpdateEntry(
            version = "1.0",
            titleRes = R.string.journal_1_0_title,
            changesRes = R.array.journal_1_0_changes,
            imageRes = R.drawable.journal_1_0,
        ),
    )
}
