package git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.core

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
import git.pef.mendelu.cz.booknest.database.entities.HistoryEntryEntity
import git.pef.mendelu.cz.booknest.database.repository.IHistoryRepository
import git.pef.mendelu.cz.booknest.sync.NetworkMonitor
import git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.models.BookHistoryItem
import git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.models.HistoryTab
import git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.models.VisitedLibraryItem
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
internal class HistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val historyRepository: IHistoryRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true))
    val uiState: StateFlow<HistoryUiState> = _uiState

    private var authListener: FirebaseAuth.AuthStateListener? = null
    private var booksListener: ListenerRegistration? = null
    private var librariesListener: ListenerRegistration? = null
    private var visitedListener: ListenerRegistration? = null
    private var currentUid: String? = null

    private var rawBooks: List<RawBookHistory> = emptyList()
    private var rawVisited: List<RawVisitedHistory> = emptyList()
    private val libraryNames: MutableMap<String, String> = mutableMapOf()

    init {
        observeLocalHistory()
        observeUser()
    }

    fun onTabSelected(tab: HistoryTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    private fun observeUser() {
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            if (uid == currentUid) return@AuthStateListener
            currentUid = uid

            clearListeners()
            rawBooks = emptyList()
            rawVisited = emptyList()
            libraryNames.clear()

            if (uid.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    books = emptyList(),
                    isLoading = false,
                    error = null
                )
                return@AuthStateListener
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            librariesListener = firestore.collection("libraries")
                .addSnapshotListener { snapshot, _ ->
                    libraryNames.clear()
                    snapshot?.documents?.forEach { doc ->
                        val name = doc.getString("name") ?: return@forEach
                        libraryNames[doc.id] = name
                    }
                    emitUi()
                }

            booksListener = firestore.collectionGroup("books")
                .whereEqualTo("addedByUid", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: context.getString(R.string.error_failed_to_load_history)
                        )
                        return@addSnapshotListener
                    }
                    rawBooks = snapshot?.documents?.mapNotNull { doc ->
                        val title = doc.getString("title") ?: return@mapNotNull null
                        val libraryId = doc.reference.parent.parent?.id
                        val addedAt = doc.getTimestamp("createdAt")?.toDate()?.time
                        RawBookHistory(doc.id, title, libraryId, addedAt)
                    } ?: emptyList()
                    emitUi()
                }

            visitedListener = firestore.collection("users")
                .document(uid)
                .collection("visitedLibraries")
                .orderBy("visitedAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: context.getString(R.string.error_failed_to_load_history)
                        )
                        return@addSnapshotListener
                    }
                    rawVisited = snapshot?.documents?.mapNotNull { doc ->
                        val name = doc.getString("libraryName") ?: return@mapNotNull null
                        val visitedAt = doc.getTimestamp("visitedAt")?.toDate()?.time
                        RawVisitedHistory(doc.id, name, visitedAt)
                    } ?: emptyList()
                    emitUi()
                }
        }
        auth.addAuthStateListener(authListener!!)
    }

    private fun emitUi() {
        val books = rawBooks.map { raw ->
            BookHistoryItem(
                id = raw.id,
                title = raw.title,
                libraryId = raw.libraryId,
                libraryName = raw.libraryId?.let { libraryNames[it] }
                    ?: context.getString(R.string.unknown_library),
                addedAtMillis = raw.addedAtMillis
            )
        }
        val visited = rawVisited.map { raw ->
            VisitedLibraryItem(
                id = raw.id,
                libraryName = raw.libraryName,
                visitedAtMillis = raw.visitedAtMillis
            )
        }
        viewModelScope.launch {
            val bookEntries = books.map { item ->
                HistoryEntryEntity(
                    id = "book_${item.id}",
                    type = "book_added",
                    bookId = item.id,
                    bookTitle = item.title,
                    libraryId = item.libraryId,
                    libraryName = item.libraryName,
                    createdAtMillis = item.addedAtMillis
                )
            }
            val visitedEntries = visited.map { item ->
                HistoryEntryEntity(
                    id = "visited_${item.id}",
                    type = "visited",
                    bookId = null,
                    bookTitle = null,
                    libraryId = item.id,
                    libraryName = item.libraryName,
                    createdAtMillis = item.visitedAtMillis
                )
            }
            historyRepository.upsertAll(bookEntries + visitedEntries)
        }
        _uiState.value = _uiState.value.copy(
            books = books,
            visited = visited,
            isLoading = false,
            error = null
        )
    }

    private fun observeLocalHistory() {
        viewModelScope.launch {
            historyRepository.observeAll().collectLatest { entries ->
                if (networkMonitor.checkOnline()) return@collectLatest
                val books = entries.filter { it.type == "book_added" }.mapNotNull { entry ->
                    val title = entry.bookTitle ?: return@mapNotNull null
                    BookHistoryItem(
                        id = entry.bookId ?: entry.id,
                        title = title,
                        libraryId = entry.libraryId,
                        libraryName = entry.libraryName ?: context.getString(R.string.unknown_library),
                        addedAtMillis = entry.createdAtMillis
                    )
                }
                val visited = entries.filter { it.type == "visited" }.mapNotNull { entry ->
                    val name = entry.libraryName ?: return@mapNotNull null
                    VisitedLibraryItem(
                        id = entry.libraryId ?: entry.id,
                        libraryName = name,
                        visitedAtMillis = entry.createdAtMillis
                    )
                }
                _uiState.value = _uiState.value.copy(
                    books = books,
                    visited = visited,
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    private fun clearListeners() {
        booksListener?.remove()
        librariesListener?.remove()
        visitedListener?.remove()
        booksListener = null
        librariesListener = null
        visitedListener = null
    }

    override fun onCleared() {
        clearListeners()
        authListener?.let { auth.removeAuthStateListener(it) }
        super.onCleared()
    }

    private data class RawBookHistory(
        val id: String,
        val title: String,
        val libraryId: String?,
        val addedAtMillis: Long?
    )

    private data class RawVisitedHistory(
        val id: String,
        val libraryName: String,
        val visitedAtMillis: Long?
    )
}
