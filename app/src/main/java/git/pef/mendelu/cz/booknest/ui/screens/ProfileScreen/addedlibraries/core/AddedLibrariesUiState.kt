package git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.addedlibraries.core

import git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.addedlibraries.models.AddedLibraryItem

data class AddedLibrariesUiState(
    val libraries: List<AddedLibraryItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
