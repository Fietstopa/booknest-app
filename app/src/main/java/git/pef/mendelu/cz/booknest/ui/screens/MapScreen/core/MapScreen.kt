package git.pef.mendelu.cz.booknest.ui.screens.MapScreen.core

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.clustering.Clustering
import git.pef.mendelu.cz.booknest.navigation.INavigationRouter
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.ui.screens.MapScreen.components.bottomsheet.MapBottomSheet
import git.pef.mendelu.cz.booknest.ui.screens.MapScreen.models.LibraryClusterItem
import git.pef.mendelu.cz.booknest.ui.theme.OnPrimaryColor
import git.pef.mendelu.cz.booknest.ui.theme.PrimaryColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MapScreen(
    navRouter: INavigationRouter
) {
    val viewModel: MapViewModel = hiltViewModel()
    val state = viewModel.uiState.collectAsState().value
    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(49.2775, 16.9983),
            12f
        )
    }
    val clusterItems = remember(state.markers) {
        state.markers.map { LibraryClusterItem(it) }
    }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    val navController = navRouter.getNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val focusLibraryId = remember { mutableStateOf<String?>(null) }
    val focusLibraryBounce = remember { mutableStateOf(false) }
    val bounceLibraryId = remember { mutableStateOf<String?>(null) }

    val permissionGranted = remember { mutableStateOf(false) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted.value = granted
        if (granted) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        viewModel.onLocationUpdated(latLng)
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                    }
                }
        }
    }

    LaunchedEffect(Unit) {
        permissionGranted.value = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (permissionGranted.value) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        viewModel.onLocationUpdated(latLng)
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                    }
                }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(currentEntry) {
        val previousHandle = navController.previousBackStackEntry?.savedStateHandle
        val currentHandle = navController.currentBackStackEntry?.savedStateHandle
        val id = previousHandle?.get<String>("focusLibraryId")
            ?: currentHandle?.get<String>("focusLibraryId")
        val bounce = previousHandle?.get<Boolean>("focusLibraryBounce")
            ?: currentHandle?.get<Boolean>("focusLibraryBounce")
            ?: false
        if (id != null) {
            focusLibraryId.value = id
            focusLibraryBounce.value = bounce
            previousHandle?.set("focusLibraryId", null)
            previousHandle?.set("focusLibraryBounce", false)
            currentHandle?.set("focusLibraryId", null)
            currentHandle?.set("focusLibraryBounce", false)
        }
    }

    LaunchedEffect(focusLibraryId.value, state.markers.size) {
        val id = focusLibraryId.value ?: return@LaunchedEffect
        val marker = viewModel.selectMarkerById(id) ?: return@LaunchedEffect
        scope.launch {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    LatLng(marker.lat, marker.lng),
                    18.5f
                ),
                durationMs = 700
            )
        }
        bounceLibraryId.value = id
        scope.launch {
            delay(400)
            scaffoldState.bottomSheetState.expand()
        }
        focusLibraryId.value = null
        focusLibraryBounce.value = false
    }


    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 48.dp,
        sheetContainerColor = Color.White,
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        sheetContent = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
            ) {
                MapBottomSheet(
                    items = state.nearbyLibraries,
                    selected = state.selectedMarker,
                    currentLocation = state.currentLocation,
                    onSelect = {
                        viewModel.onSelectMarker(it)
                        bounceLibraryId.value = it.id
                        scope.launch {
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newLatLngZoom(
                                    LatLng(it.lat, it.lng),
                                    18.5f
                                ),
                                durationMs = 500
                            )
                        }
                        scope.launch { scaffoldState.bottomSheetState.expand() }
                    },
                    onClearSelection = { viewModel.onClearSelection() },
                    onSeeBooks = { library ->
                        viewModel.onClearSelection()
                        navRouter.navigateToBooksList(library.id)
                    },
                    onScanIsbn = { library ->
                        viewModel.onClearSelection()
                        navRouter.navigateToBookScanner(library.id)
                    }
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = permissionGranted.value
                ),
                uiSettings = MapUiSettings(
                    compassEnabled = false,
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false
                )
            ) {
                Clustering(
                    items = clusterItems,
                    onClusterItemClick = { item ->
                        val marker = item.marker
                        viewModel.onSelectMarker(marker)
                        bounceLibraryId.value = marker.id
                        scope.launch {
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newLatLngZoom(
                                    LatLng(marker.lat, marker.lng),
                                    18.5f
                                ),
                                durationMs = 500
                            )
                        }
                        scope.launch { scaffoldState.bottomSheetState.expand() }
                        true
                    },
                    onClusterClick = { cluster ->
                        val nextZoom = cameraPositionState.position.zoom + 1f
                        scope.launch {
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newLatLngZoom(
                                    cluster.position,
                                    nextZoom
                                ),
                                durationMs = 500
                            )
                        }
                        true
                    },
                    clusterItemContent = {
                        Image(
                            painter = painterResource(R.drawable.custom_location_marker),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    clusterContent = { cluster ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PrimaryColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Text(
                                text = cluster.size.toString(),
                                color = OnPrimaryColor
                            )
                        }
                    }
                )
            }

            FloatingActionButton(
                onClick = { navRouter.navigateToAddLibrary() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 80.dp),
                containerColor = Color(0xFFE0EE6A)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_add_library),
                    modifier = Modifier.size(24.dp)
                )
            }

            FloatingActionButton(
                onClick = {
                    if (permissionGranted.value) {
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location ->
                                if (location != null) {
                                    val latLng = LatLng(location.latitude, location.longitude)
                                    viewModel.onLocationUpdated(latLng)
                                    cameraPositionState.position =
                                        CameraPosition.fromLatLngZoom(latLng, 16f)
                                }
                            }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 150.dp),
                containerColor = Color.White,
                contentColor = Color(0xFF5D492B)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = stringResource(R.string.cd_my_location),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
