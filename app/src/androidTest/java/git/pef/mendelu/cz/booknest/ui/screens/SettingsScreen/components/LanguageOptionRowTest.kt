package git.pef.mendelu.cz.booknest.ui.screens.SettingsScreen.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LanguageOptionRowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun languageOptionRow_displaysTitle_andHandlesClick() {
        val clicked = mutableStateOf(false)
        composeRule.setContent {
            LanguageOptionRow(
                title = "English",
                selected = false,
                onSelect = { clicked.value = true }
            )
        }

        composeRule.onNodeWithText("English").assertIsDisplayed().performClick()
        assertTrue(clicked.value)
    }
}
