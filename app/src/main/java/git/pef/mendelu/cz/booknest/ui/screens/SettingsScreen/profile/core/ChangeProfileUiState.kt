package git.pef.mendelu.cz.booknest.ui.screens.SettingsScreen.profile.core

data class ChangeProfileUiState(
    val nickname: String = "",
    val currentPhotoUrl: String? = null,
    val selectedPhotoUri: android.net.Uri? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)
