package git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.savedbooks.core

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.savedbooks.models.SavedBookItem
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
internal class SavedBooksViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(SavedBooksUiState())
    val uiState: StateFlow<SavedBooksUiState> = _uiState

    private var authListener: FirebaseAuth.AuthStateListener? = null
    private var savedBooksListener: ListenerRegistration? = null
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
                _uiState.value = SavedBooksUiState(
                    books = emptyList(),
                    isLoading = false,
                    error = null
                )
                return@AuthStateListener
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            savedBooksListener = firestore.collection("users")
                .document(uid)
                .collection("savedBooks")
                .orderBy("savedAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message
                                ?: context.getString(R.string.error_failed_to_load_saved_books)
                        )
                        return@addSnapshotListener
                    }
                    val books = snapshot?.documents?.mapNotNull { doc ->
                        val title = doc.getString("title") ?: return@mapNotNull null
                        val authors = doc.get("authors") as? List<String> ?: emptyList()
                        val thumbnail = doc.getString("thumbnail")
                        SavedBookItem(doc.id, title, authors, thumbnail)
                    } ?: emptyList()
                    _uiState.value = SavedBooksUiState(
                        books = books,
                        isLoading = false,
                        error = null
                    )
                }
        }
        auth.addAuthStateListener(authListener!!)
    }

    private fun clearListeners() {
        savedBooksListener?.remove()
        savedBooksListener = null
    }

    override fun onCleared() {
        clearListeners()
        authListener?.let { auth.removeAuthStateListener(it) }
        super.onCleared()
    }
}
