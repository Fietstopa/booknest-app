package git.pef.mendelu.cz.booknest.ui.screens.MapScreen.components.bottomsheet

import android.location.Geocoder
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FieldValue
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import git.pef.mendelu.cz.booknest.database.BooknestDatabase
import git.pef.mendelu.cz.booknest.database.entities.AddedBookEntity
import git.pef.mendelu.cz.booknest.database.entities.HistoryEntryEntity
import git.pef.mendelu.cz.booknest.database.entities.PendingBookEntity
import git.pef.mendelu.cz.booknest.sync.NetworkMonitor
import git.pef.mendelu.cz.booknest.communication.CommunicationResult
import git.pef.mendelu.cz.booknest.communication.GoogleBookItem
import git.pef.mendelu.cz.booknest.communication.IBooksRemoteRepository
import git.pef.mendelu.cz.booknest.R
import java.util.UUID
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
internal class MapBottomSheetViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val booksRepository: IBooksRemoteRepository,
    private val database: BooknestDatabase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(MapBottomSheetUiState())
    val uiState: StateFlow<MapBottomSheetUiState> = _uiState.asStateFlow()
    private var commentsRegistration: ListenerRegistration? = null
    private var likesRegistration: ListenerRegistration? = null

    fun loadAddress(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(
            isAddressLoading = true,
            city = null,
            street = null
        )
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()
                }.getOrNull()
            }
            val city = result?.locality ?: result?.subAdminArea
            val street = result?.thoroughfare?.let { name ->
                val number = result?.subThoroughfare
                if (!number.isNullOrBlank()) "$name $number" else name
            }
            _uiState.value = _uiState.value.copy(
                city = city,
                street = street,
                isAddressLoading = false
            )
        }
    }

    fun searchBooks(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchError = context.getString(R.string.error_search_query_empty)
            )
            return
        }
        _uiState.value = _uiState.value.copy(isSearching = true, searchError = null)
        viewModelScope.launch {
            when (val response = booksRepository.searchBooks(trimmed)) {
                is CommunicationResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        searchResults = response.data.items ?: emptyList(),
                        isSearching = false
                    )
                }
                is CommunicationResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        searchError = response.error.message
                            ?: context.getString(R.string.error_search_failed),
                        isSearching = false
                    )
                }
                is CommunicationResult.ConnectionError -> {
                    _uiState.value = _uiState.value.copy(
                        searchError = context.getString(R.string.error_connection),
                        isSearching = false
                    )
                }
                is CommunicationResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        searchError = response.exception.message
                            ?: context.getString(R.string.error_unexpected),
                        isSearching = false
                    )
                }
            }
        }
    }

    fun saveBook(
        libraryId: String,
        item: GoogleBookItem,
        addedByUid: String?,
        addedByName: String?
    ) {
        val info = item.volumeInfo
        val thumbnail = info.imageLinks?.thumbnail?.replace("http://", "https://")
        val createdAt = System.currentTimeMillis()
        val fallbackUser = auth.currentUser
        val finalUid = addedByUid ?: fallbackUser?.uid
        val finalName = addedByName
            ?: fallbackUser?.displayName?.takeIf { it.isNotBlank() }
            ?: fallbackUser?.email?.substringBefore("@")
        val bookData = mapOf(
            "volumeId" to item.id,
            "title" to (info.title ?: context.getString(R.string.unknown_title)),
            "authors" to (info.authors ?: emptyList<String>()),
            "thumbnail" to thumbnail,
            "addedByUid" to finalUid,
            "addedByName" to finalName,
            "createdAt" to FieldValue.serverTimestamp()
        )
        _uiState.value = _uiState.value.copy(saveError = null, saveSuccess = false)
        if (!networkMonitor.checkOnline()) {
            val localId = "local_${UUID.randomUUID()}"
            viewModelScope.launch {
                database.pendingBookDao().upsert(
                    PendingBookEntity(
                        localId = localId,
                        libraryId = libraryId,
                        bookId = item.id,
                        title = info.title ?: context.getString(R.string.unknown_title),
                        authors = info.authors ?: emptyList(),
                        thumbnail = thumbnail,
                        addedByUid = finalUid,
                        addedByName = finalName,
                        createdAtMillis = createdAt
                    )
                )
                database.addedBookDao().upsert(
                    AddedBookEntity(
                        id = item.id,
                        title = info.title ?: context.getString(R.string.unknown_title),
                        authors = info.authors ?: emptyList(),
                        thumbnail = thumbnail,
                        libraryId = libraryId,
                        addedByUid = finalUid,
                        createdAtMillis = createdAt
                    )
                )
                database.historyDao().upsert(
                    HistoryEntryEntity(
                        id = "book_${item.id}",
                        type = "book_added",
                        bookId = item.id,
                        bookTitle = info.title ?: context.getString(R.string.unknown_title),
                        libraryId = libraryId,
                        libraryName = null,
                        createdAtMillis = createdAt
                    )
                )
            }
            _uiState.value = _uiState.value.copy(saveSuccess = true)
            return
        }
        firestore.collection("libraries")
            .document(libraryId)
            .collection("books")
            .document(item.id)
            .set(bookData)
            .addOnSuccessListener {
                viewModelScope.launch {
                    if (!finalUid.isNullOrBlank()) {
                        firestore.collection("users")
                            .document(finalUid)
                            .collection("addedBooks")
                            .document(item.id)
                            .set(
                                bookData + mapOf(
                                    "libraryId" to libraryId
                                )
                            )
                    }
                    database.addedBookDao().upsert(
                        AddedBookEntity(
                            id = item.id,
                            title = info.title ?: context.getString(R.string.unknown_title),
                            authors = info.authors ?: emptyList(),
                            thumbnail = thumbnail,
                            libraryId = libraryId,
                            addedByUid = finalUid,
                            createdAtMillis = createdAt
                        )
                    )
                    database.historyDao().upsert(
                        HistoryEntryEntity(
                            id = "book_${item.id}",
                            type = "book_added",
                            bookId = item.id,
                            bookTitle = info.title ?: context.getString(R.string.unknown_title),
                            libraryId = libraryId,
                            libraryName = null,
                            createdAtMillis = createdAt
                        )
                    )
                }
                _uiState.value = _uiState.value.copy(saveSuccess = true)
            }
            .addOnFailureListener { error ->
                _uiState.value = _uiState.value.copy(
                    saveError = error.message ?: context.getString(R.string.error_failed_to_save_book),
                    saveSuccess = false
                )
            }
    }

    fun consumeSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    fun observeComments(libraryId: String) {
        commentsRegistration?.remove()
        _uiState.value = _uiState.value.copy(
            comments = emptyList(),
            commentText = "",
            commentError = null
        )
        commentsRegistration = firestore.collection("libraries")
            .document(libraryId)
            .collection("comments")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val comments = snapshot?.documents?.mapNotNull { doc ->
                    val text = doc.getString("text") ?: return@mapNotNull null
                    val authorName = doc.getString("authorName")
                        ?: context.getString(R.string.unknown_author)
                    val authorUid = doc.getString("authorUid")
                    val createdAt = doc.getTimestamp("createdAt")?.toDate()?.time
                    LibraryComment(doc.id, text, authorName, authorUid, createdAt)
                } ?: emptyList()
                _uiState.value = _uiState.value.copy(comments = comments)
            }
    }

    fun updateCommentText(text: String) {
        _uiState.value = _uiState.value.copy(commentText = text, commentError = null)
    }

    fun submitComment(libraryId: String, authorUid: String?, authorName: String?) {
        val message = _uiState.value.commentText.trim()
        if (message.isBlank()) {
            _uiState.value = _uiState.value.copy(
                commentError = context.getString(R.string.error_write_comment_first)
            )
            return
        }
        _uiState.value = _uiState.value.copy(isCommentSending = true, commentError = null)
        val data = mapOf(
            "text" to message,
            "authorUid" to authorUid,
            "authorName" to (authorName ?: context.getString(R.string.unknown_author)),
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("libraries")
            .document(libraryId)
            .collection("comments")
            .add(data)
            .addOnSuccessListener {
                _uiState.value = _uiState.value.copy(
                    isCommentSending = false,
                    commentText = ""
                )
            }
            .addOnFailureListener { error ->
                _uiState.value = _uiState.value.copy(
                    isCommentSending = false,
                    commentError = error.message
                        ?: context.getString(R.string.error_send_comment_failed)
                )
            }
    }

    fun observeLikes(libraryId: String, uid: String?) {
        likesRegistration?.remove()
        _uiState.value = _uiState.value.copy(
            likesCount = 0,
            isLiked = false,
            likeError = null
        )
        likesRegistration = firestore.collection("libraries")
            .document(libraryId)
            .collection("likes")
            .addSnapshotListener { snapshot, _ ->
                val count = snapshot?.size() ?: 0
                val liked = uid?.let { userId ->
                    snapshot?.documents?.any { it.id == userId } == true
                } ?: false
                _uiState.value = _uiState.value.copy(
                    likesCount = count,
                    isLiked = liked
                )
            }
    }

    fun toggleLike(libraryId: String, uid: String?) {
        if (uid.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                likeError = context.getString(R.string.error_sign_in_to_like)
            )
            return
        }
        _uiState.value = _uiState.value.copy(likeError = null)
        val docRef = firestore.collection("libraries")
            .document(libraryId)
            .collection("likes")
            .document(uid)
        docRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    docRef.delete()
                } else {
                    docRef.set(
                        mapOf(
                            "uid" to uid,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }
            .addOnFailureListener { error ->
                _uiState.value = _uiState.value.copy(
                    likeError = error.message ?: context.getString(R.string.error_update_like_failed)
                )
            }
    }

    override fun onCleared() {
        commentsRegistration?.remove()
        likesRegistration?.remove()
        super.onCleared()
    }
}
