package git.pef.mendelu.cz.booknest.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.navigation.Destination
import git.pef.mendelu.cz.booknest.navigation.INavigationRouter
import git.pef.mendelu.cz.booknest.navigation.NavGraph
import git.pef.mendelu.cz.booknest.navigation.NavigationRouterImpl
import git.pef.mendelu.cz.booknest.ui.components.BaseScreen
import git.pef.mendelu.cz.booknest.ui.theme.BooknestTheme

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        fun createIntent(context: android.content.Context): android.content.Intent {
            return android.content.Intent(context, MainActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BooknestTheme {
                MainContent()
            }
        }
    }
}

private data class BottomBarItem(
    val route: String,
    val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun MainContent() {
    val navController = rememberNavController()
    val navRouter: INavigationRouter = remember { NavigationRouterImpl(navController) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomBarItem(Destination.MapScreen.route, R.string.map_title, Icons.Filled.Map),
        BottomBarItem(Destination.HistoryScreen.route, R.string.history_title, Icons.Filled.History),
        BottomBarItem(Destination.ProfileScreen.route, R.string.profile_title, Icons.Filled.AccountCircle)
    )

    val title = when {
        currentRoute?.startsWith(Destination.BooksListScreen.route.substringBefore("/{")) == true ->
            stringResource(R.string.books_list_title)
        currentRoute?.startsWith(Destination.BookDetailScreen.route.substringBefore("/{")) == true ->
            stringResource(R.string.book_detail_title)
        currentRoute?.startsWith(Destination.BookScannerScreen.route.substringBefore("/{")) == true ->
            stringResource(R.string.scan_title)
        currentRoute == Destination.AddLibraryScreen.route -> stringResource(R.string.add_library_title)
        currentRoute == Destination.SettingsScreen.route -> stringResource(R.string.settings_title)
        currentRoute == Destination.SettingsLanguageScreen.route -> stringResource(R.string.settings_language_title)
        currentRoute == Destination.ChangeProfileScreen.route -> stringResource(R.string.change_profile_title)
        currentRoute == Destination.SavedBooksScreen.route -> stringResource(R.string.saved_books_list_title)
        currentRoute == Destination.AddedBooksScreen.route -> stringResource(R.string.added_books_list_title)
        currentRoute == Destination.AddedLibrariesScreen.route -> stringResource(R.string.added_libraries_list_title)
        currentRoute == Destination.MapScreen.route -> stringResource(R.string.map_title)
        currentRoute == Destination.HistoryScreen.route -> stringResource(R.string.history_title)
        currentRoute == Destination.ProfileScreen.route -> stringResource(R.string.profile_title)
        else -> ""
    }

    val showBottomBar = currentRoute == Destination.MapScreen.route ||
        currentRoute == Destination.HistoryScreen.route ||
        currentRoute == Destination.ProfileScreen.route

    BaseScreen(
        title = title,
        showTopBar = true,
        onBack = if (showBottomBar) null else navRouter::returnBack,
        topBarActions = {
            if (currentRoute == Destination.ProfileScreen.route) {
                IconButton(onClick = navRouter::navigateToSettings) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.cd_settings)
                    )
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    items.forEach { item ->
                        val selected = currentRoute == item.route
                        val label = stringResource(item.labelRes)
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                when (item.route) {
                                    Destination.MapScreen.route -> navRouter.navigateToMap()
                                    Destination.HistoryScreen.route -> navRouter.navigateToHistory()
                                    Destination.ProfileScreen.route -> navRouter.navigateToProfile()
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                unselectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                indicatorColor = MaterialTheme.colorScheme.secondary
                            ),
                            icon = { Icon(item.icon, contentDescription = label) },
                            label = { Text(text = label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavGraph(
                startNavigation = Destination.MapScreen.route,
                navHostController = navController,
                navRouter = navRouter
            )
        }
    }
}
