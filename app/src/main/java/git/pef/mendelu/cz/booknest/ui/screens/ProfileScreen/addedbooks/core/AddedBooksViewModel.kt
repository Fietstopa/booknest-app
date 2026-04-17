package git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.addedbooks.core

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.database.entities.AddedBookEntity
import git.pef.mendelu.cz.booknest.database.repository.IAddedBooksRepository
import git.pef.mendelu.cz.booknest.sync.NetworkMonitor
import git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.addedbooks.models.AddedBookItem
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
internal class AddedBooksViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val addedBooksRepository: IAddedBooksRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(AddedBooksUiState())
    val uiState: StateFlow<AddedBooksUiState> = _uiState

    private var authListener: FirebaseAuth.AuthStateListener? = null
    private var addedBooksListener: ListenerRegistration? = null
    private var currentUid: String? = null
    private var didBackfill = false

    init {
        observeLocalCache()
        observeUser()
    }

    private fun observeUser() {
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            if (uid == currentUid) return@AuthStateListener
            currentUid = uid
            didBackfill = false

            clearListeners()
            if (uid.isNullOrBlank()) {
                _uiState.value = AddedBooksUiState(
                    books = emptyList(),
                    isLoading = false,
                    error = null
                )
                return@AuthStateListener
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            addedBooksListener = firestore.collection("users")
                .document(uid)
                .collection("addedBooks")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message
                                ?: context.getString(R.string.error_failed_to_load_added_books)
                        )
                        return@addSnapshotListener
                    }
                    val books = snapshot?.documents?.mapNotNull { doc ->
                        val title = doc.getString("title") ?: return@mapNotNull null
                        val authors = doc.get("authors") as? List<String> ?: emptyList()
                        val thumbnail = doc.getString("thumbnail")
                        AddedBookItem(doc.id, title, authors, thumbnail)
                    } ?: emptyList()
                    if (books.isEmpty() && !didBackfill) {
                        didBackfill = true
                        firestore.collectionGroup("books")
                            .whereEqualTo("addedByUid", uid)
                            .get()
                            .addOnSuccessListener { legacySnapshot ->
                                val legacyBooks = legacySnapshot.documents.mapNotNull { doc ->
                                    val title = doc.getString("title") ?: return@mapNotNull null
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
                                    AddedBookItem(doc.id, title, authors, thumbnail)
                                }
                                _uiState.value = AddedBooksUiState(
                                    books = legacyBooks,
                                    isLoading = false,
                                    error = null
                                )
                            }
                            .addOnFailureListener {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = it.message
                                        ?: context.getString(R.string.error_failed_to_load_added_books)
                                )
                            }
                        return@addSnapshotListener
                    }
                    viewModelScope.launch {
                        val entities = books.map { item ->
                            AddedBookEntity(
                                id = item.id,
                                title = item.title,
                                authors = item.authors,
                                thumbnail = item.thumbnail,
                                libraryId = null,
                                addedByUid = uid,
                                createdAtMillis = null
                            )
                        }
                        addedBooksRepository.upsertAll(entities)
                    }
                    _uiState.value = AddedBooksUiState(
                        books = books,
                        isLoading = false,
                        error = null
                    )
                }
        }
        auth.addAuthStateListener(authListener!!)
    }

    private fun observeLocalCache() {
        viewModelScope.launch {
            addedBooksRepository.observeAll().collectLatest { cached ->
                if (networkMonitor.checkOnline()) return@collectLatest
                val books = cached.map {
                    AddedBookItem(
                        id = it.id,
                        title = it.title,
                        authors = it.authors,
                        thumbnail = it.thumbnail
                    )
                }
                _uiState.value = AddedBooksUiState(
                    books = books,
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    private fun clearListeners() {
        addedBooksListener?.remove()
        addedBooksListener = null
    }

    override fun onCleared() {
        clearListeners()
        authListener?.let { auth.removeAuthStateListener(it) }
        super.onCleared()
    }
}
