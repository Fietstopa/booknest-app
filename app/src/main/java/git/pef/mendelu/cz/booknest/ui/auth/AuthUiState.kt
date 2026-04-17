package git.pef.mendelu.cz.booknest.ui.auth

data class AuthUiState(
    val email: String = "",
    val nickname: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
