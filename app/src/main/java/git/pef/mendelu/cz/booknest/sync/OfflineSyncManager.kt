package git.pef.mendelu.cz.booknest.sync

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import git.pef.mendelu.cz.booknest.database.BooknestDatabase
import git.pef.mendelu.cz.booknest.database.entities.AddedBookEntity
import git.pef.mendelu.cz.booknest.database.entities.HistoryEntryEntity
import git.pef.mendelu.cz.booknest.database.entities.NearbyLibraryEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Singleton
class OfflineSyncManager @Inject constructor(
    private val database: BooknestDatabase,
    private val networkMonitor: NetworkMonitor,
    private val localImageStore: LocalImageStore
) {
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        networkMonitor.start()
        scope.launch { syncPendingIfOnline() }
        scope.launch {
            networkMonitor.isOnline.collectLatest { online ->
                if (online) {
                    syncPendingIfOnline()
                }
            }
        }
    }

    suspend fun syncPendingIfOnline() {
        if (!networkMonitor.checkOnline()) return
        syncPendingLibraries()
        syncPendingBooks()
    }

    private suspend fun syncPendingLibraries() {
        val pending = database.pendingLibraryDao().getAll()
        pending.forEach { item ->
            val remoteId = UUID.randomUUID().toString()
            val imageFile = localImageStore.asFile(item.imageLocalPath)
            val imageRef = storage.reference.child("libraries/${remoteId}_${imageFile.name}")
            val downloadUrl = imageRef.putFile(android.net.Uri.fromFile(imageFile))
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw task.exception ?: IllegalStateException("Image upload failed.")
                    }
                    imageRef.downloadUrl
                }
                .awaitResult()
            firestore.collection("libraries")
                .document(remoteId)
                .set(
                    mapOf(
                        "name" to item.name,
                        "latitude" to item.latitude,
                        "longitude" to item.longitude,
                        "imageUrl" to downloadUrl.toString(),
                        "size" to item.size,
                        "createdByUid" to item.createdByUid,
                        "createdByName" to item.createdByName,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                .awaitResult()
            database.pendingLibraryDao().deleteById(item.localId)
            database.nearbyLibraryDao().upsertAll(
                listOf(
                    NearbyLibraryEntity(
                        id = remoteId,
                        name = item.name,
                        latitude = item.latitude,
                        longitude = item.longitude,
                        size = item.size,
                        imageUrl = downloadUrl.toString(),
                        imageLocalPath = item.imageLocalPath,
                        createdByName = item.createdByName,
                        distanceMeters = 0.0,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
            )
        }
    }

    private suspend fun syncPendingBooks() {
        val pending = database.pendingBookDao().getAll()
        pending.forEach { item ->
            firestore.collection("libraries")
                .document(item.libraryId)
                .collection("books")
                .document(item.bookId)
                .set(
                    mapOf(
                        "volumeId" to item.bookId,
                        "title" to item.title,
                        "authors" to item.authors,
                        "thumbnail" to item.thumbnail,
                        "addedByUid" to item.addedByUid,
                        "addedByName" to item.addedByName,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                .awaitResult()
            if (!item.addedByUid.isNullOrBlank()) {
                firestore.collection("users")
                    .document(item.addedByUid)
                    .collection("addedBooks")
                    .document(item.bookId)
                    .set(
                        mapOf(
                            "volumeId" to item.bookId,
                            "title" to item.title,
                            "authors" to item.authors,
                            "thumbnail" to item.thumbnail,
                            "addedByUid" to item.addedByUid,
                            "addedByName" to item.addedByName,
                            "libraryId" to item.libraryId,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                    .awaitResult()
            }
            database.pendingBookDao().deleteById(item.localId)
            database.addedBookDao().upsert(
                AddedBookEntity(
                    id = item.bookId,
                    title = item.title,
                    authors = item.authors,
                    thumbnail = item.thumbnail,
                    libraryId = item.libraryId,
                    addedByUid = item.addedByUid,
                    createdAtMillis = item.createdAtMillis
                )
            )
            database.historyDao().upsert(
                HistoryEntryEntity(
                    id = "book_${item.bookId}",
                    type = "book_added",
                    bookId = item.bookId,
                    bookTitle = item.title,
                    libraryId = item.libraryId,
                    libraryName = null,
                    createdAtMillis = item.createdAtMillis
                )
            )
        }
    }

    private suspend fun <T> Task<T>.awaitResult(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) {} }
            addOnFailureListener { error -> continuation.cancel(error) }
        }
    }
}
