package git.pef.mendelu.cz.booknest.ui.screens.MapScreen.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import git.pef.mendelu.cz.booknest.database.BooknestDatabase
import git.pef.mendelu.cz.booknest.database.entities.NearbyLibraryEntity
import git.pef.mendelu.cz.booknest.database.entities.PendingLibraryEntity
import git.pef.mendelu.cz.booknest.sync.LocalImageStore
import git.pef.mendelu.cz.booknest.sync.NetworkMonitor
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import com.google.android.gms.tasks.Task
import android.location.Location

@HiltViewModel
internal class AddLibraryViewModel @Inject constructor(
    private val database: BooknestDatabase,
    private val networkMonitor: NetworkMonitor,
    private val localImageStore: LocalImageStore
) : ViewModel() {
    private val saveScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun saveLibrary(
        name: String,
        latitude: Double,
        longitude: Double,
        size: String,
        imageUri: Uri,
        createdByUid: String?,
        createdByName: String?,
        currentLat: Double?,
        currentLng: Double?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        saveScope.launch {
            val localPath = localImageStore.copyToLocalFile(imageUri, "library")
            val createdAt = System.currentTimeMillis()
            if (!networkMonitor.checkOnline()) {
                enqueueLibrary(
                    localPath,
                    name,
                    latitude,
                    longitude,
                    size,
                    createdByUid,
                    createdByName,
                    currentLat,
                    currentLng,
                    createdAt
                )
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
                return@launch
            }
            try {
                val remoteId = UUID.randomUUID().toString()
                val imageFile = localImageStore.asFile(localPath)
                val imageRef = storage.reference.child("libraries/${remoteId}_${imageFile.name}")
                val downloadUrl = imageRef.putFile(Uri.fromFile(imageFile))
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
                            "name" to name,
                            "latitude" to latitude,
                            "longitude" to longitude,
                            "imageUrl" to downloadUrl.toString(),
                            "size" to size,
                            "createdByUid" to createdByUid,
                            "createdByName" to createdByName,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    )
                    .awaitResult()
                val distance = currentLat?.let { lat ->
                    currentLng?.let { lng ->
                        val dist = FloatArray(1)
                        Location.distanceBetween(lat, lng, latitude, longitude, dist)
                        dist[0].toDouble()
                    }
                } ?: 0.0
                database.nearbyLibraryDao().upsertAll(
                    listOf(
                        NearbyLibraryEntity(
                            id = remoteId,
                            name = name,
                            latitude = latitude,
                            longitude = longitude,
                            size = size,
                            imageUrl = downloadUrl.toString(),
                            imageLocalPath = localPath,
                            createdByName = createdByName,
                            distanceMeters = distance,
                            updatedAtMillis = createdAt
                        )
                    )
                )
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (error: Exception) {
                enqueueLibrary(
                    localPath,
                    name,
                    latitude,
                    longitude,
                    size,
                    createdByUid,
                    createdByName,
                    currentLat,
                    currentLng,
                    createdAt
                )
                withContext(Dispatchers.Main) {
                    onError(error.message ?: "Failed to save library.")
                }
            }
        }
    }

    private suspend fun enqueueLibrary(
        localPath: String,
        name: String,
        latitude: Double,
        longitude: Double,
        size: String,
        createdByUid: String?,
        createdByName: String?,
        currentLat: Double?,
        currentLng: Double?,
        createdAt: Long
    ) {
        val localId = "local_${UUID.randomUUID()}"
        database.pendingLibraryDao().upsert(
            PendingLibraryEntity(
                localId = localId,
                name = name,
                latitude = latitude,
                longitude = longitude,
                size = size,
                imageLocalPath = localPath,
                createdByUid = createdByUid,
                createdByName = createdByName,
                createdAtMillis = createdAt
            )
        )
        val distance = currentLat?.let { lat ->
            currentLng?.let { lng ->
                val dist = FloatArray(1)
                Location.distanceBetween(lat, lng, latitude, longitude, dist)
                dist[0].toDouble()
            }
        } ?: 0.0
        database.nearbyLibraryDao().upsertAll(
            listOf(
                NearbyLibraryEntity(
                    id = localId,
                    name = name,
                    latitude = latitude,
                    longitude = longitude,
                    size = size,
                    imageUrl = null,
                    imageLocalPath = localPath,
                    createdByName = createdByName,
                    distanceMeters = distance,
                    updatedAtMillis = createdAt
                )
            )
        )
    }

    private suspend fun <T> Task<T>.awaitResult(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) {} }
            addOnFailureListener { error -> continuation.cancel(error) }
        }
    }
}
