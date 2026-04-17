package git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.addedlibraries.core

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.addedlibraries.models.AddedLibraryItem
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
internal class AddedLibrariesViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(AddedLibrariesUiState())
    val uiState: StateFlow<AddedLibrariesUiState> = _uiState

    private var authListener: FirebaseAuth.AuthStateListener? = null
    private var librariesListener: ListenerRegistration? = null
    private var currentUid: String? = null

    init {
        observeUser()
    }

    private fun observeUser() {
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            if (uid == currentUid) return@AuthStateListener
            currentUid = uid

            clearListeners()
            if (uid.isNullOrBlank()) {
                _uiState.value = AddedLibrariesUiState(
                    libraries = emptyList(),
                    isLoading = false,
                    error = null
                )
                return@AuthStateListener
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            librariesListener = firestore.collection("libraries")
                .whereEqualTo("createdByUid", uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message
                                ?: context.getString(R.string.error_failed_to_load_added_libraries)
                        )
                        return@addSnapshotListener
                    }
                    val libraries = snapshot?.documents?.mapNotNull { doc ->
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val imageUrl = doc.getString("imageUrl")
                        AddedLibraryItem(doc.id, name, imageUrl)
                    } ?: emptyList()
                    _uiState.value = AddedLibrariesUiState(
                        libraries = libraries,
                        isLoading = false,
                        error = null
                    )
                }
        }
        auth.addAuthStateListener(authListener!!)
    }

    private fun clearListeners() {
        librariesListener?.remove()
        librariesListener = null
    }

    override fun onCleared() {
        clearListeners()
        authListener?.let { auth.removeAuthStateListener(it) }
        super.onCleared()
    }
}
