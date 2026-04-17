package git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun formatHistoryDate(millis: Long?, unknownLabel: String): String {
    if (millis == null) return unknownLabel
    val formatter = SimpleDateFormat("d.M.yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}
