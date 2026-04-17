package git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.models

internal enum class HistoryTab {
    Visited,
    BooksAdded
}

internal data class BookHistoryItem(
    val id: String,
    val title: String,
    val libraryId: String?,
    val libraryName: String,
    val addedAtMillis: Long?
)

internal data class VisitedLibraryItem(
    val id: String,
    val libraryName: String,
    val visitedAtMillis: Long?
)
