package git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import git.pef.mendelu.cz.booknest.navigation.INavigationRouter
import git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.components.HistoryListItem
import git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.components.HistoryTabs
import git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.components.VisitedHistoryListItem
import git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.models.HistoryTab
import git.pef.mendelu.cz.booknest.R

@Composable
internal fun HistoryScreen(
    navRouter: INavigationRouter
) {
    val viewModel: HistoryViewModel = hiltViewModel()
    val state = viewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F1B16))
    ) {
        HistoryTabs(
            selectedTab = state.selectedTab,
            onSelect = viewModel::onTabSelected
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2E4CD))
        ) {
            when (state.selectedTab) {
                HistoryTab.Visited -> {
                    if (state.visited.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_visited_libraries),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5D492B),
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            itemsIndexed(state.visited) { index, item ->
                                VisitedHistoryListItem(item = item)
                                if (index != state.visited.lastIndex) {
                                    HorizontalDivider(
                                        thickness = 1.dp,
                                        color = Color(0xFFBDAA8E)
                                    )
                                }
                            }
                        }
                    }
                }
                HistoryTab.BooksAdded -> {
                    if (state.books.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_books_added),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5D492B),
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            itemsIndexed(state.books) { index, item ->
                                HistoryListItem(item = item)
                                if (index != state.books.lastIndex) {
                                    HorizontalDivider(
                                        thickness = 1.dp,
                                        color = Color(0xFFBDAA8E)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
