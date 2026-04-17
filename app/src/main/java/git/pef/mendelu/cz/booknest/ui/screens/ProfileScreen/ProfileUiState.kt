package git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen

data class ProfileUiState(
    val nickname: String = "",
    val photoUrl: String? = null,
    val addedBooksCount: Int = 0,
    val addedLibrariesCount: Int = 0,
    val savedBooks: List<SavedBook> = emptyList(),
    val addedBooks: List<AddedBook> = emptyList(),
    val addedLibraries: List<AddedLibrary> = emptyList()
)

data class SavedBook(
    val id: String,
    val title: String,
    val authors: List<String>,
    val thumbnail: String?
)

data class AddedBook(
    val id: String,
    val title: String,
    val authors: List<String>,
    val thumbnail: String?
)

data class AddedLibrary(
    val id: String,
    val name: String,
    val imageUrl: String?
)
