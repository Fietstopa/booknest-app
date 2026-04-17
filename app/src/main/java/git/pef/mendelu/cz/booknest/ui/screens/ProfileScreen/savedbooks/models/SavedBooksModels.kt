package git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.savedbooks.models

data class SavedBookItem(
    val id: String,
    val title: String,
    val authors: List<String>,
    val thumbnail: String?
)
