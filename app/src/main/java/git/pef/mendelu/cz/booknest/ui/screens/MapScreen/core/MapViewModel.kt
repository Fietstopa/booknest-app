package git.pef.mendelu.cz.booknest.ui.screens.MapScreen.core

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import git.pef.mendelu.cz.booknest.database.entities.NearbyLibraryEntity
import git.pef.mendelu.cz.booknest.database.repository.INearbyLibrariesRepository
import git.pef.mendelu.cz.booknest.sync.NetworkMonitor
import git.pef.mendelu.cz.booknest.ui.screens.MapScreen.models.LibraryMarker
import git.pef.mendelu.cz.booknest.ui.screens.MapScreen.models.NearbyLibrary
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
internal class MapViewModel @Inject constructor(
    private val nearbyRepository: INearbyLibrariesRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var registration: ListenerRegistration? = null
    private val visitedIds = mutableSetOf<String>()
    private var authListener: FirebaseAuth.AuthStateListener? = null

    init {
        observeAuth()
        observeNetwork()
        observeLocalCache()
        if (networkMonitor.checkOnline()) {
            startRemoteListener()
        }
    }

    fun onLocationUpdated(location: LatLng) {
        val current = _uiState.value
        _uiState.value = current.copy(
            currentLocation = location,
            nearbyLibraries = computeNearby(current.markers, location)
        )
        if (networkMonitor.checkOnline()) {
            cacheNearbyLibraries(current.markers, location)
        }
        trackVisitedLibraries(current.markers, location)
    }

    fun onSelectMarker(marker: LibraryMarker) {
        _uiState.value = _uiState.value.copy(selectedMarker = marker)
    }

    fun selectMarkerById(id: String): LibraryMarker? {
        val marker = _uiState.value.markers.firstOrNull { it.id == id }
        if (marker != null) {
            _uiState.value = _uiState.value.copy(selectedMarker = marker)
        }
        return marker
    }

    fun onClearSelection() {
        _uiState.value = _uiState.value.copy(selectedMarker = null)
    }

    private fun computeNearby(
        markers: List<LibraryMarker>,
        location: LatLng?
    ): List<NearbyLibrary> {
        if (location == null) {
            return markers.map { marker ->
                NearbyLibrary(marker, Float.NaN)
            }
        }
        val results = mutableListOf<NearbyLibrary>()
        markers.forEach { marker ->
            val dist = FloatArray(1)
            Location.distanceBetween(
                location.latitude,
                location.longitude,
                marker.lat,
                marker.lng,
                dist
            )
            if (dist[0] <= 10000f) {
                results.add(NearbyLibrary(marker, dist[0]))
            }
        }
        return results.sortedBy { it.distanceMeters }.take(20)
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnline.collectLatest { online ->
                if (online) {
                    startRemoteListener()
                } else {
                    stopRemoteListener()
                }
            }
        }
    }

    private fun observeLocalCache() {
        viewModelScope.launch {
            nearbyRepository.observeAll().collectLatest { cached ->
                if (!networkMonitor.checkOnline()) {
                    val markers = cached.map {
                        LibraryMarker(
                            id = it.id,
                            name = it.name,
                            lat = it.latitude,
                            lng = it.longitude,
                            imageUrl = it.imageUrl,
                            imageLocalPath = it.imageLocalPath,
                            size = it.size,
                            createdByName = it.createdByName
                        )
                    }
                    val current = _uiState.value
                    val updatedSelected = current.selectedMarker?.let { selected ->
                        markers.firstOrNull { it.id == selected.id }
                    }
                    _uiState.value = current.copy(
                        markers = markers,
                        selectedMarker = updatedSelected,
                        nearbyLibraries = computeNearby(markers, current.currentLocation)
                    )
                }
            }
        }
    }

    private fun startRemoteListener() {
        if (registration != null) return
        registration = firestore.collection("libraries")
            .addSnapshotListener { snapshot, error ->
                if (snapshot == null || error != null) {
                    return@addSnapshotListener
                }
                val markers = snapshot.documents.mapNotNull { doc ->
                    val lat = doc.getDouble("latitude")
                    val lng = doc.getDouble("longitude")
                    val name = doc.getString("name")
                    val imageUrl = doc.getString("imageUrl")
                    val size = doc.getString("size") ?: "M"
                    val createdByName = doc.getString("createdByName")
                    if (lat == null || lng == null || name.isNullOrBlank()) {
                        null
                    } else {
                        LibraryMarker(
                            doc.id,
                            name,
                            lat,
                            lng,
                            imageUrl,
                            null,
                            size,
                            createdByName
                        )
                    }
                }
                val current = _uiState.value
                val updatedSelected = current.selectedMarker?.let { selected ->
                    markers.firstOrNull { it.id == selected.id }
                }
                _uiState.value = current.copy(
                    markers = markers,
                    selectedMarker = updatedSelected,
                    nearbyLibraries = computeNearby(markers, current.currentLocation)
                )
                cacheNearbyLibraries(markers, current.currentLocation)
            }
    }

    private fun stopRemoteListener() {
        registration?.remove()
        registration = null
    }

    private fun cacheNearbyLibraries(markers: List<LibraryMarker>, location: LatLng?) {
        val entities = markers.map { marker ->
            val dist = FloatArray(1)
            if (location != null) {
                Location.distanceBetween(
                    location.latitude,
                    location.longitude,
                    marker.lat,
                    marker.lng,
                    dist
                )
            } else {
                dist[0] = 0f
            }
            NearbyLibraryEntity(
                id = marker.id,
                name = marker.name,
                latitude = marker.lat,
                longitude = marker.lng,
                size = marker.size,
                imageUrl = marker.imageUrl,
                imageLocalPath = marker.imageLocalPath,
                createdByName = marker.createdByName,
                distanceMeters = dist[0].toDouble(),
                updatedAtMillis = System.currentTimeMillis()
            )
        }.filter { location == null || it.distanceMeters <= 10000.0 }
        viewModelScope.launch {
            nearbyRepository.upsertAll(entities)
        }
    }

    private fun trackVisitedLibraries(markers: List<LibraryMarker>, location: LatLng) {
        val uid = auth.currentUser?.uid ?: return
        if (!networkMonitor.checkOnline()) return
        markers.forEach { marker ->
            if (visitedIds.contains(marker.id)) return@forEach
            val dist = FloatArray(1)
            Location.distanceBetween(
                location.latitude,
                location.longitude,
                marker.lat,
                marker.lng,
                dist
            )
            if (dist[0] <= 20f) {
                visitedIds.add(marker.id)
                firestore.collection("users")
                    .document(uid)
                    .collection("visitedLibraries")
                    .document(marker.id)
                    .set(
                        mapOf(
                            "libraryId" to marker.id,
                            "libraryName" to marker.name,
                            "visitedAt" to FieldValue.serverTimestamp()
                        )
                    )
            }
        }
    }

    override fun onCleared() {
        stopRemoteListener()
        authListener?.let { auth.removeAuthStateListener(it) }
        super.onCleared()
    }

    private fun observeAuth() {
        authListener = FirebaseAuth.AuthStateListener {
            visitedIds.clear()
        }
        auth.addAuthStateListener(authListener!!)
    }
}
