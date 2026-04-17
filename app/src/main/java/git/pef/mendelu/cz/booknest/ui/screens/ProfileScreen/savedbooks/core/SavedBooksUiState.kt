package git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.savedbooks.core

import git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.savedbooks.models.SavedBookItem

data class SavedBooksUiState(
    val books: List<SavedBookItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
