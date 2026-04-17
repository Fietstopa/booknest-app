package git.pef.mendelu.cz.booknest.ui.screens.SettingsScreen.core

import git.pef.mendelu.cz.booknest.ui.screens.SettingsScreen.models.AppLanguage

internal data class SettingsUiState(
    val isLoading: Boolean = false,
    val language: AppLanguage = AppLanguage.English
)
