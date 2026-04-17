package git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.addedbooks.core

import git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.addedbooks.models.AddedBookItem

data class AddedBooksUiState(
    val books: List<AddedBookItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
