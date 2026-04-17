package git.pef.mendelu.cz.booknest.ui.screens.SettingsScreen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import androidx.test.platform.app.InstrumentationRegistry
import git.pef.mendelu.cz.booknest.ui.screens.SettingsScreen.core.SettingsScreen
import git.pef.mendelu.cz.booknest.navigation.INavigationRouter
import git.pef.mendelu.cz.booknest.R
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsScreen_showsRows_andHandlesClicks() {
        val fakeRouter = FakeNavRouter()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val changeProfileLabel = context.getString(R.string.change_profile_title)
        val languageLabel = context.getString(R.string.settings_language)
        composeRule.setContent {
            SettingsScreen(navRouter = fakeRouter)
        }

        composeRule.onNodeWithText(changeProfileLabel).assertIsDisplayed().performClick()
        composeRule.onNodeWithText(languageLabel).assertIsDisplayed().performClick()

        assertTrue(fakeRouter.changeProfileCalled)
        assertTrue(fakeRouter.languageCalled)
    }
}

private class FakeNavRouter : INavigationRouter {
    var changeProfileCalled = false
    var languageCalled = false

    override fun navigateToMap() {}
    override fun navigateToHistory() {}
    override fun navigateToProfile() {}
    override fun navigateToSettings() {}
    override fun navigateToSettingsLanguage() {
        languageCalled = true
    }
    override fun navigateToChangeProfile() {
        changeProfileCalled = true
    }
    override fun navigateToSavedBooks() {}
    override fun navigateToAddedBooks() {}
    override fun navigateToAddedLibraries() {}
    override fun navigateToAddLibrary() {}
    override fun navigateToBooksList(libraryId: String) {}
    override fun navigateToBookDetail(bookId: String) {}
    override fun navigateToBookScanner(libraryId: String) {}
    override fun returnBack() {}
    override fun getNavController(): NavController {
        error("Not used in SettingsScreen test.")
    }
}
