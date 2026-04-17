package git.pef.mendelu.cz.booknest.navigation

import androidx.navigation.NavController

interface INavigationRouter {
    fun navigateToMap()
    fun navigateToHistory()
    fun navigateToProfile()
    fun navigateToSettings()
    fun navigateToSettingsLanguage()
    fun navigateToChangeProfile()
    fun navigateToSavedBooks()
    fun navigateToAddedBooks()
    fun navigateToAddedLibraries()
    fun navigateToAddLibrary()
    fun navigateToBooksList(libraryId: String)
    fun navigateToBookDetail(bookId: String)
    fun navigateToBookScanner(libraryId: String)
    fun returnBack()
    fun getNavController(): NavController
}
