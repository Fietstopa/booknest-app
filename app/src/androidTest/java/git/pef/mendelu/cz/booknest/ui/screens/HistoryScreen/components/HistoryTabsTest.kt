package git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.models.HistoryTab
import org.junit.Rule
import org.junit.Test

class HistoryTabsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun selectingTab_updatesSelectionState() {
        val selected = mutableStateOf(HistoryTab.BooksAdded)
        composeRule.setContent {
            HistoryTabs(
                selectedTab = selected.value,
                onSelect = { selected.value = it }
            )
        }

        composeRule.onNodeWithTag("HistoryTab_BooksAdded").assertIsSelected()
        composeRule.onNodeWithTag("HistoryTab_Visited").performClick()
        composeRule.onNodeWithTag("HistoryTab_Visited").assertIsSelected()
    }
}
