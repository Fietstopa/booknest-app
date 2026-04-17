package git.pef.mendelu.cz.booknest.ui.screens.MapScreen.core

import com.google.android.gms.maps.model.LatLng
import git.pef.mendelu.cz.booknest.ui.screens.MapScreen.models.LibraryMarker
import git.pef.mendelu.cz.booknest.ui.screens.MapScreen.models.NearbyLibrary

internal data class MapUiState(
    val markers: List<LibraryMarker> = emptyList(),
    val selectedMarker: LibraryMarker? = null,
    val nearbyLibraries: List<NearbyLibrary> = emptyList(),
    val currentLocation: LatLng? = null
)
