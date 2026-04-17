package git.pef.mendelu.cz.booknest.ui.screens.SettingsScreen.core

import androidx.lifecycle.ViewModel
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.lifecycle.HiltViewModel
import git.pef.mendelu.cz.booknest.ui.screens.SettingsScreen.models.AppLanguage
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
internal class SettingsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(
        SettingsUiState(language = currentLanguage())
    )
    val uiState: StateFlow<SettingsUiState> = _uiState

    fun updateLanguage(language: AppLanguage) {
        _uiState.value = _uiState.value.copy(language = language)
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language.code)
        )
    }

    private fun currentLanguage(): AppLanguage {
        val locales = AppCompatDelegate.getApplicationLocales()
        val code = locales.get(0)?.language ?: return AppLanguage.English
        return AppLanguage.values().firstOrNull { it.code == code } ?: AppLanguage.English
    }
}
