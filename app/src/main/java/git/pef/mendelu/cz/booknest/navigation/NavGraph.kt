package git.pef.mendelu.cz.booknest.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.core.HistoryScreen
import git.pef.mendelu.cz.booknest.ui.screens.SettingsScreen.core.SettingsScreen
import git.pef.mendelu.cz.booknest.ui.screens.MapScreen.books.BookDetailScreen
import git.pef.mendelu.cz.booknest.ui.screens.MapScreen.books.LibraryBookScannerScreen
import git.pef.mendelu.cz.booknest.ui.screens.MapScreen.books.LibraryBooksScreen
import git.pef.mendelu.cz.booknest.ui.screens.MapScreen.core.MapScreen
import git.pef.mendelu.cz.booknest.ui.screens.MapScreen.library.AddLibraryScreen
import git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.ProfileScreen
import git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.savedbooks.core.SavedBooksScreen
import git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.addedbooks.core.AddedBooksScreen
import git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.addedlibraries.core.AddedLibrariesScreen
import git.pef.mendelu.cz.booknest.ui.screens.SettingsScreen.language.core.LanguageSettingsScreen
import git.pef.mendelu.cz.booknest.ui.screens.SettingsScreen.profile.core.ChangeProfileScreen

@Composable
fun NavGraph(
    startNavigation: String,
    navHostController: NavHostController = rememberNavController(),
    navRouter: INavigationRouter = remember {
        NavigationRouterImpl(navHostController)
    }
) {
    NavHost(navController = navHostController, startDestination = startNavigation) {
        composable(route = Destination.MapScreen.route) {
            MapScreen(navRouter)
        }
        composable(route = Destination.HistoryScreen.route) {
            HistoryScreen(navRouter)
        }
        composable(route = Destination.ProfileScreen.route) {
            ProfileScreen(navRouter)
        }
        composable(route = Destination.SettingsScreen.route) {
            SettingsScreen(navRouter)
        }
        composable(route = Destination.SettingsLanguageScreen.route) {
            LanguageSettingsScreen(navRouter)
        }
        composable(route = Destination.ChangeProfileScreen.route) {
            ChangeProfileScreen(navRouter)
        }
        composable(route = Destination.SavedBooksScreen.route) {
            SavedBooksScreen(navRouter)
        }
        composable(route = Destination.AddedBooksScreen.route) {
            AddedBooksScreen(navRouter)
        }
        composable(route = Destination.AddedLibrariesScreen.route) {
            AddedLibrariesScreen(navRouter)
        }
        composable(route = Destination.AddLibraryScreen.route) {
            AddLibraryScreen(
                navRouter = navRouter,
                onClose = navRouter::returnBack
            )
        }
        composable(
            route = Destination.BooksListScreen.route,
            arguments = listOf(navArgument("libraryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val libraryId = backStackEntry.arguments?.getString("libraryId") ?: return@composable
            LibraryBooksScreen(
                libraryId = libraryId,
                navRouter = navRouter
            )
        }
        composable(
            route = Destination.BookDetailScreen.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: return@composable
            BookDetailScreen(
                navRouter = navRouter,
                bookId = bookId
            )
        }
        composable(
            route = Destination.BookScannerScreen.route,
            arguments = listOf(navArgument("libraryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val libraryId = backStackEntry.arguments?.getString("libraryId") ?: return@composable
            LibraryBookScannerScreen(
                navRouter = navRouter,
                libraryId = libraryId,
                onBack = navRouter::returnBack
            )
        }
    }
}
