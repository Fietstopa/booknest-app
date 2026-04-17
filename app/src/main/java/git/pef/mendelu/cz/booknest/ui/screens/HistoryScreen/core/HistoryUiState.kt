package git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.core

import git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.models.BookHistoryItem
import git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.models.HistoryTab
import git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.models.VisitedLibraryItem

internal data class HistoryUiState(
    val selectedTab: HistoryTab = HistoryTab.BooksAdded,
    val books: List<BookHistoryItem> = emptyList(),
    val visited: List<VisitedLibraryItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
