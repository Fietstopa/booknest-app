package git.pef.mendelu.cz.booknest.ui.screens.MapScreen.models

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

internal data class LibraryMarker(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val imageUrl: String?,
    val imageLocalPath: String?,
    val size: String,
    val createdByName: String?
)

internal data class NearbyLibrary(
    val marker: LibraryMarker,
    val distanceMeters: Float
)

internal data class LibraryBook(
    val id: String,
    val title: String,
    val authors: List<String>,
    val thumbnail: String?
)

internal data class LibraryClusterItem(
    val marker: LibraryMarker
) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(marker.lat, marker.lng)
    override fun getTitle(): String = marker.name
    override fun getSnippet(): String = marker.size
    override fun getZIndex(): Float? = null
}
