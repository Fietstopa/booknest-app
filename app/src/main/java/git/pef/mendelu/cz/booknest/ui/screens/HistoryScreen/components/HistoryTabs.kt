package git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.platform.testTag
import git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.models.HistoryTab
import git.pef.mendelu.cz.booknest.R

@Composable
internal fun HistoryTabs(
    selectedTab: HistoryTab,
    onSelect: (HistoryTab) -> Unit
) {
    val tabs = listOf(
        HistoryTab.Visited to stringResource(R.string.history_tab_visited),
        HistoryTab.BooksAdded to stringResource(R.string.history_tab_books_added)
    )
    Row(
        modifier = Modifier
            .background(Color(0xFFD6C7AE))
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        tabs.forEachIndexed { index, (tab, label) ->
            val isSelected = selectedTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(tab) }
                    .testTag("HistoryTab_${tab.name}")
                    .semantics { selected = isSelected },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) Color(0xFF3C2E1A) else Color(0xFF6B5A40)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .height(3.dp)
                            .width(32.dp)
                            .background(if (isSelected) Color(0xFF5D492B) else Color.Transparent)
                            .testTag("HistoryTabIndicator_${tab.name}")
                    )
                }
            }
            if (index == 0) {
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(Color(0xFFBDAA8E))
    )
}
