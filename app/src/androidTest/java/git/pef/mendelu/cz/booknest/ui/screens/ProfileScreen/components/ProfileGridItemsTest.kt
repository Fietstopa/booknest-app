package git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.addedbooks.components.AddedBooksGridItem
import git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.addedbooks.models.AddedBookItem
import git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.savedbooks.components.SavedBooksGridItem
import git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.savedbooks.models.SavedBookItem
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileGridItemsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun addedBooksGridItem_displaysTitle_andHandlesClick() {
        var clicked = false
        val item = AddedBookItem(
            id = "book1",
            title = "Clean Code",
            authors = listOf("Robert C. Martin"),
            thumbnail = null
        )
        composeRule.setContent {
            AddedBooksGridItem(item = item, onClick = { clicked = true })
        }

        composeRule.onNodeWithText("Clean Code").assertIsDisplayed().performClick()
        assertTrue(clicked)
    }

    @Test
    fun savedBooksGridItem_displaysTitle_andHandlesClick() {
        var clicked = false
        val item = SavedBookItem(
            id = "book2",
            title = "Effective Kotlin",
            authors = listOf("Marcin Moskala"),
            thumbnail = null
        )
        composeRule.setContent {
            SavedBooksGridItem(item = item, onClick = { clicked = true })
        }

        composeRule.onNodeWithText("Effective Kotlin").assertIsDisplayed().performClick()
        assertTrue(clicked)
    }
}
