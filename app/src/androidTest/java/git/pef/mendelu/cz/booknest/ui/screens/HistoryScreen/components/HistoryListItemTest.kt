package git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.models.BookHistoryItem
import git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.models.VisitedLibraryItem
import org.junit.Rule
import org.junit.Test

class HistoryListItemTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bookHistoryItem_displaysTitleAndLibrary() {
        val item = BookHistoryItem(
            id = "book1",
            title = "Clean Code",
            libraryId = "lib1",
            libraryName = "Main Library",
            addedAtMillis = null
        )
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            HistoryListItem(item = item)
        }

        composeRule.onNodeWithText("Clean Code").assertIsDisplayed()
        composeRule.onNodeWithText("Main Library").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.unknown_date)).assertIsDisplayed()
    }

    @Test
    fun visitedHistoryItem_displaysLibraryName() {
        val item = VisitedLibraryItem(
            id = "lib1",
            libraryName = "Central Library",
            visitedAtMillis = null
        )
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            VisitedHistoryListItem(item = item)
        }

        composeRule.onNodeWithText("Central Library").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.unknown_date)).assertIsDisplayed()
    }
}
