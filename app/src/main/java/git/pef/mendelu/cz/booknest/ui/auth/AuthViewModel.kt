package git.pef.mendelu.cz.booknest.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import git.pef.mendelu.cz.booknest.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateEmail(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun updateNickname(value: String) {
        _uiState.value = _uiState.value.copy(nickname = value, errorMessage = null)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun updateConfirmPassword(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, errorMessage = null)
    }

    fun signInWithEmail(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            setError(context.getString(R.string.error_email_password_required))
            return
        }
        setLoading(true)
        auth.signInWithEmailAndPassword(state.email.trim(), state.password)
            .addOnCompleteListener { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    setError(task.exception?.message ?: context.getString(R.string.error_login_failed))
                }
            }
    }

    fun registerWithEmail(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.nickname.isBlank() || state.email.isBlank() || state.password.isBlank() || state.confirmPassword.isBlank()) {
            setError(context.getString(R.string.error_register_fields_required))
            return
        }
        if (state.password != state.confirmPassword) {
            setError(context.getString(R.string.error_passwords_do_not_match))
            return
        }
        setLoading(true)
        auth.createUserWithEmailAndPassword(state.email.trim(), state.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(state.nickname.trim())
                        .build()
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            setLoading(false)
                            if (updateTask.isSuccessful) {
                                onSuccess()
                            } else {
                                setError(
                                    updateTask.exception?.message
                                        ?: context.getString(R.string.error_registration_failed)
                                )
                            }
                        } ?: run {
                        setLoading(false)
                        setError(context.getString(R.string.error_registration_failed))
                    }
                } else {
                    setLoading(false)
                    setError(task.exception?.message ?: context.getString(R.string.error_registration_failed))
                }
            }
    }

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit) {
        setLoading(true)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    setError(task.exception?.message ?: context.getString(R.string.error_google_sign_in_failed))
                }
            }
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    private fun setLoading(loading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = loading)
    }
}
