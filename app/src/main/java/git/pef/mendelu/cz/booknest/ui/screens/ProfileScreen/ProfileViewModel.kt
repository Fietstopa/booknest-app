package git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import git.pef.mendelu.cz.booknest.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var booksListener: ListenerRegistration? = null
    private var librariesListener: ListenerRegistration? = null
    private var savedBooksListener: ListenerRegistration? = null
    private var authListener: FirebaseAuth.AuthStateListener? = null
    private var currentUid: String? = null
    private var didBackfill = false

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            val nickname = user?.displayName?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.profile_guest_name)
            val photoUrl = user?.photoUrl?.toString()?.takeIf { it.isNotBlank() }
            _uiState.value = _uiState.value.copy(nickname = nickname, photoUrl = photoUrl)

            val uid = user?.uid
            if (uid == currentUid) return@AuthStateListener
            currentUid = uid
            didBackfill = false
            if (uid.isNullOrBlank()) {
                clearListeners()
                _uiState.value = _uiState.value.copy(
                    addedBooksCount = 0,
                    addedLibrariesCount = 0,
                    savedBooks = emptyList(),
                    addedBooks = emptyList(),
                    addedLibraries = emptyList()
                )
                return@AuthStateListener
            }

            clearListeners()
            booksListener = firestore.collection("users")
                .document(uid)
                .collection("addedBooks")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if ((snapshot == null || snapshot.isEmpty) && !didBackfill) {
                        didBackfill = true
                        firestore.collectionGroup("books")
                            .whereEqualTo("addedByUid", uid)
                            .get()
                            .addOnSuccessListener { legacySnapshot ->
                                legacySnapshot.documents.forEach { doc ->
                                    val title = doc.getString("title") ?: return@forEach
                                    val authors =
                                        doc.get("authors") as? List<String> ?: emptyList()
                                    val thumbnail = doc.getString("thumbnail")
                                    val createdAt = doc.getTimestamp("createdAt")
                                    val payload = mapOf(
                                        "volumeId" to doc.getString("volumeId"),
                                        "title" to title,
                                        "authors" to authors,
                                        "thumbnail" to thumbnail,
                                        "addedByUid" to uid,
                                        "addedByName" to doc.getString("addedByName"),
                                        "libraryId" to doc.reference.parent.parent?.id,
                                        "createdAt" to createdAt
                                    )
                                    firestore.collection("users")
                                        .document(uid)
                                        .collection("addedBooks")
                                        .document(doc.id)
                                        .set(payload)
                                }
                            }
                        return@addSnapshotListener
                    }
                    val addedBooks = snapshot?.documents?.mapNotNull { doc ->
                        val title = doc.getString("title") ?: return@mapNotNull null
                        val authors = doc.get("authors") as? List<String> ?: emptyList()
                        val thumbnail = doc.getString("thumbnail")
                        AddedBook(doc.id, title, authors, thumbnail)
                    } ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        addedBooksCount = snapshot?.size() ?: 0,
                        addedBooks = addedBooks
                    )
                }
            librariesListener = firestore.collection("libraries")
                .whereEqualTo("createdByUid", uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val libraries = snapshot?.documents?.mapNotNull { doc ->
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val imageUrl = doc.getString("imageUrl")
                        AddedLibrary(doc.id, name, imageUrl)
                    } ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        addedLibrariesCount = snapshot?.size() ?: 0,
                        addedLibraries = libraries
                    )
                }
            savedBooksListener = firestore.collection("users")
                .document(uid)
                .collection("savedBooks")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val updated = snapshot?.documents?.mapNotNull { doc ->
                        val title = doc.getString("title") ?: return@mapNotNull null
                        val authors = doc.get("authors") as? List<String> ?: emptyList()
                        val thumbnail = doc.getString("thumbnail")
                        SavedBook(doc.id, title, authors, thumbnail)
                    } ?: emptyList()
                    _uiState.value = _uiState.value.copy(savedBooks = updated)
                }
        }
        auth.addAuthStateListener(authListener!!)
    }

    private fun clearListeners() {
        booksListener?.remove()
        librariesListener?.remove()
        savedBooksListener?.remove()
        booksListener = null
        librariesListener = null
        savedBooksListener = null
    }

    override fun onCleared() {
        clearListeners()
        authListener?.let { auth.removeAuthStateListener(it) }
        super.onCleared()
    }
}
