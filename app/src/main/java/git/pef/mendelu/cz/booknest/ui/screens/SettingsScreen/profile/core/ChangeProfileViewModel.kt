package git.pef.mendelu.cz.booknest.ui.screens.SettingsScreen.profile.core

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import git.pef.mendelu.cz.booknest.R
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlinx.coroutines.launch

@HiltViewModel
internal class ChangeProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val _uiState = MutableStateFlow(ChangeProfileUiState())
    val uiState: StateFlow<ChangeProfileUiState> = _uiState

    init {
        val user = auth.currentUser
        val currentName = user?.displayName?.takeIf { it.isNotBlank() }.orEmpty()
        val currentPhoto = user?.photoUrl?.toString()
        _uiState.value = _uiState.value.copy(
            nickname = currentName,
            currentPhotoUrl = currentPhoto
        )
    }

    fun updateNickname(value: String) {
        _uiState.value = _uiState.value.copy(nickname = value, error = null, success = false)
    }

    fun updatePhoto(uri: Uri?) {
        _uiState.value = _uiState.value.copy(selectedPhotoUri = uri, error = null, success = false)
    }

    fun saveProfile() {
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = _uiState.value.copy(
                error = context.getString(R.string.error_profile_not_signed_in)
            )
            return
        }
        viewModelScope.launch {
            val state = _uiState.value
            val nickname = state.nickname.trim()
            val finalName = if (nickname.isNotBlank()) {
                nickname
            } else {
                user.displayName?.takeIf { it.isNotBlank() }.orEmpty()
            }
            if (finalName.isBlank() && state.selectedPhotoUri == null) {
                _uiState.value = _uiState.value.copy(
                    error = context.getString(R.string.error_profile_name_required)
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, success = false)
            try {
                val photoUrl = if (state.selectedPhotoUri != null) {
                    val photoRef = storage.reference.child(
                        "users/${user.uid}/profile_${System.currentTimeMillis()}.jpg"
                    )
                    val downloadUrl = photoRef.putFile(state.selectedPhotoUri)
                        .continueWithTask { task ->
                            if (!task.isSuccessful) {
                                throw task.exception
                                    ?: IllegalStateException("Upload failed.")
                            }
                            photoRef.downloadUrl
                        }
                        .awaitResult()
                    downloadUrl.toString()
                } else {
                    state.currentPhotoUrl
                }
                val updates = UserProfileChangeRequest.Builder()
                    .setDisplayName(finalName)
                    .apply {
                        if (!photoUrl.isNullOrBlank()) {
                            setPhotoUri(Uri.parse(photoUrl))
                        }
                    }
                    .build()
                user.updateProfile(updates).awaitResult()
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    success = true,
                    currentPhotoUrl = photoUrl,
                    selectedPhotoUri = null
                )
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = error.message
                        ?: context.getString(R.string.error_profile_update_failed)
                )
            }
        }
    }

    fun consumeSuccess() {
        _uiState.value = _uiState.value.copy(success = false)
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitResult(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) {} }
            addOnFailureListener { error -> continuation.cancel(error) }
        }
    }
}
