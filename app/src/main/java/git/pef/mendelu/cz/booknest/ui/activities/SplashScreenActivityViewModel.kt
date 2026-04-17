package git.pef.mendelu.cz.booknest.ui.activities

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import git.pef.mendelu.cz.booknest.data.UserPrefs
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class SplashScreenActivityViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(SplashScreenUiState())
    val uiState: StateFlow<SplashScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            UserPrefs.isFirstRun(context).collectLatest { firstRun ->
                _uiState.value = if (firstRun) {
                    SplashScreenUiState(runForFirstTime = true)
                } else {
                    SplashScreenUiState(continueToApp = true)
                }
            }
        }
    }
}
