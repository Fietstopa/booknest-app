package git.pef.mendelu.cz.booknest.ui.screens.SettingsScreen.language.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.navigation.INavigationRouter
import git.pef.mendelu.cz.booknest.ui.screens.SettingsScreen.components.LanguageOptionRow
import git.pef.mendelu.cz.booknest.ui.screens.SettingsScreen.core.SettingsViewModel
import git.pef.mendelu.cz.booknest.ui.screens.SettingsScreen.models.AppLanguage

@Composable
internal fun LanguageSettingsScreen(
    navRouter: INavigationRouter
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state = viewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2E4CD))
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_language),
            color = Color(0xFF5D492B)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LanguageOptionRow(
            title = stringResource(R.string.language_english),
            selected = state.language == AppLanguage.English,
            onSelect = { viewModel.updateLanguage(AppLanguage.English) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        LanguageOptionRow(
            title = stringResource(R.string.language_czech),
            selected = state.language == AppLanguage.Czech,
            onSelect = { viewModel.updateLanguage(AppLanguage.Czech) }
        )
    }
}
