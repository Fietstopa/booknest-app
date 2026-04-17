package git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.addedbooks.models

data class AddedBookItem(
    val id: String,
    val title: String,
    val authors: List<String>,
    val thumbnail: String?
)
