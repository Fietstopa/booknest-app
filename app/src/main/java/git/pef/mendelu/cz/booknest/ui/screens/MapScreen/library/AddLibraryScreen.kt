package git.pef.mendelu.cz.booknest.ui.screens.MapScreen.library

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.navigation.INavigationRouter
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
internal fun AddLibraryScreen(
    navRouter: INavigationRouter,
    onClose: () -> Unit
) {
    val viewModel: AddLibraryViewModel = hiltViewModel()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    val permissionGranted = remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(49.2775, 16.9983), 16f)
    }
    val defaultLibraryName = stringResource(R.string.default_library_name)
    val libraryName = remember(defaultLibraryName) { mutableStateOf(defaultLibraryName) }
    val selectedSize = remember { mutableStateOf("M") }
    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val lastKnownLat = remember { mutableStateOf<Double?>(null) }
    val lastKnownLng = remember { mutableStateOf<Double?>(null) }
    val isSaving = remember { mutableStateOf(false) }
    val nameRequiredMessage = stringResource(R.string.error_name_required)
    val imageRequiredMessage = stringResource(R.string.error_image_required)
    val uploadFailedMessage = stringResource(R.string.error_failed_to_upload_image)
    val saveFailedMessage = stringResource(R.string.error_failed_to_save_library)

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri.value = uri
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted.value = granted
        if (granted) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        lastKnownLat.value = location.latitude
                        lastKnownLng.value = location.longitude
                        val latLng = LatLng(location.latitude, location.longitude)
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 17f)
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
                        lastKnownLat.value = location.latitude
                        lastKnownLng.value = location.longitude
                        val latLng = LatLng(location.latitude, location.longitude)
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 17f)
                    }
                }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val selectedTarget = remember { mutableStateOf(cameraPositionState.position.target) }
    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving }
            .distinctUntilChanged()
            .collectLatest { moving ->
                if (!moving) {
                    selectedTarget.value = cameraPositionState.position.target
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = permissionGranted.value
                ),
                uiSettings = MapUiSettings(
                    compassEnabled = true,
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false
                ),
                onMapClick = { latLng ->
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                        latLng,
                        cameraPositionState.position.zoom
                    )
                }
            )

            Image(
                painter = painterResource(R.drawable.custom_location_marker),
                contentDescription = stringResource(R.string.cd_selected_location),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.pick_image))
            }

            selectedImageUri.value?.let { uri ->
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = uri,
                    contentDescription = stringResource(R.string.cd_selected_image),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = libraryName.value,
                onValueChange = { libraryName.value = it },
                label = { Text(stringResource(R.string.library_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = stringResource(R.string.library_size_label))

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                val sizeOptions = listOf(
                    "S" to "S",
                    "M" to "M",
                    "XL" to "XL"
                )
                sizeOptions.forEach { (size, label) ->
                    val isSelected = selectedSize.value == size
                    Button(
                        onClick = { selectedSize.value = size },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = if (size != "XL") 8.dp else 0.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF5D492B) else Color(0xFFE9DFC8),
                            contentColor = if (isSelected) Color.White else Color(0xFF3C2E1A)
                        )
                    ) {
                        Text(text = label)
                    }
                }
            }

            errorMessage.value?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = message, color = Color(0xFFB3261E))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isSaving.value) {
                        return@Button
                    }
                    val name = libraryName.value.trim()
                    if (name.isEmpty()) {
                        errorMessage.value = nameRequiredMessage
                        return@Button
                    }
                    val imageUri = selectedImageUri.value
                    if (imageUri == null) {
                        errorMessage.value = imageRequiredMessage
                        return@Button
                    }
                    isSaving.value = true
                    val target = selectedTarget.value
                    val createdByUid = currentUser?.uid
                    val createdByName = currentUser?.displayName
                        ?.takeIf { it.isNotBlank() }
                        ?: currentUser?.email?.substringBefore("@")
                    viewModel.saveLibrary(
                        name = name,
                        latitude = target.latitude,
                        longitude = target.longitude,
                        size = selectedSize.value,
                        imageUri = imageUri,
                        createdByUid = createdByUid,
                        createdByName = createdByName,
                        currentLat = lastKnownLat.value,
                        currentLng = lastKnownLng.value,
                        onSuccess = {},
                        onError = { errorMessage.value = it }
                    )
                    onClose()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving.value
            ) {
                Text(text = stringResource(R.string.save))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
