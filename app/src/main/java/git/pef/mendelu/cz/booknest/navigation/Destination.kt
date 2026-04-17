package git.pef.mendelu.cz.booknest.navigation

import android.net.Uri

sealed class Destination(val route: String) {
    object MapScreen : Destination("map_screen")
    object HistoryScreen : Destination("history_screen")
    object ProfileScreen : Destination("profile_screen")
    object SettingsScreen : Destination("settings_screen")
    object SettingsLanguageScreen : Destination("settings_language_screen")
    object ChangeProfileScreen : Destination("change_profile_screen")
    object SavedBooksScreen : Destination("saved_books_screen")
    object AddedBooksScreen : Destination("added_books_screen")
    object AddedLibrariesScreen : Destination("added_libraries_screen")
    object AddLibraryScreen : Destination("add_library_screen")
    object BooksListScreen : Destination("books_list_screen/{libraryId}") {
        fun createRoute(libraryId: String): String {
            return "books_list_screen/${Uri.encode(libraryId)}"
        }
    }
    object BookDetailScreen : Destination("book_detail_screen/{bookId}") {
        fun createRoute(bookId: String): String {
            return "book_detail_screen/${Uri.encode(bookId)}"
        }
    }
    object BookScannerScreen : Destination("book_scanner_screen/{libraryId}") {
        fun createRoute(libraryId: String): String {
            return "book_scanner_screen/${Uri.encode(libraryId)}"
        }
    }
}
