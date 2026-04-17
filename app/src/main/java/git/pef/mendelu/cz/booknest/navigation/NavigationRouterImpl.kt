package git.pef.mendelu.cz.booknest.navigation

import androidx.navigation.NavController

class NavigationRouterImpl(
    private val navController: NavController
) : INavigationRouter {

    override fun navigateToMap() {
        navController.navigate(route = Destination.MapScreen.route) {
            launchSingleTop = true
        }
    }

    override fun navigateToHistory() {
        navController.navigate(route = Destination.HistoryScreen.route) {
            launchSingleTop = true
        }
    }

    override fun navigateToProfile() {
        navController.navigate(route = Destination.ProfileScreen.route) {
            launchSingleTop = true
        }
    }

    override fun navigateToSettings() {
        navController.navigate(route = Destination.SettingsScreen.route) {
            launchSingleTop = true
        }
    }

    override fun navigateToSettingsLanguage() {
        navController.navigate(route = Destination.SettingsLanguageScreen.route) {
            launchSingleTop = true
        }
    }

    override fun navigateToChangeProfile() {
        navController.navigate(route = Destination.ChangeProfileScreen.route) {
            launchSingleTop = true
        }
    }

    override fun navigateToSavedBooks() {
        navController.navigate(route = Destination.SavedBooksScreen.route) {
            launchSingleTop = true
        }
    }

    override fun navigateToAddedBooks() {
        navController.navigate(route = Destination.AddedBooksScreen.route) {
            launchSingleTop = true
        }
    }

    override fun navigateToAddedLibraries() {
        navController.navigate(route = Destination.AddedLibrariesScreen.route) {
            launchSingleTop = true
        }
    }

    override fun navigateToAddLibrary() {
        navController.navigate(route = Destination.AddLibraryScreen.route)
    }

    override fun navigateToBooksList(libraryId: String) {
        navController.navigate(route = Destination.BooksListScreen.createRoute(libraryId))
    }

    override fun navigateToBookDetail(bookId: String) {
        navController.navigate(route = Destination.BookDetailScreen.createRoute(bookId))
    }

    override fun navigateToBookScanner(libraryId: String) {
        navController.navigate(route = Destination.BookScannerScreen.createRoute(libraryId))
    }

    override fun returnBack() {
        navController.popBackStack()
    }

    override fun getNavController(): NavController {
        return navController
    }
}
