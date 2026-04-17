package git.pef.mendelu.cz.booknest.ui.screens.MapScreen.components.bottomsheet

import git.pef.mendelu.cz.booknest.ui.theme.PrimaryColor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.google.android.gms.maps.model.LatLng
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton
import com.google.firebase.auth.FirebaseAuth
import git.pef.mendelu.cz.booknest.communication.GoogleBookItem
import git.pef.mendelu.cz.booknest.ui.screens.MapScreen.models.LibraryMarker
import git.pef.mendelu.cz.booknest.ui.screens.MapScreen.models.NearbyLibrary
import git.pef.mendelu.cz.booknest.ui.screens.MapScreen.utils.formatDistance
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import git.pef.mendelu.cz.booknest.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DividerColor = PrimaryColor.copy(alpha = 0.25f)
private val CommentDateFormat = SimpleDateFormat("d.M.yyyy HH:mm", Locale.getDefault())



@Composable
internal fun MapBottomSheet(
    items: List<NearbyLibrary>,
    selected: LibraryMarker?,
    currentLocation: LatLng?,
    onSelect: (LibraryMarker) -> Unit,
    onClearSelection: () -> Unit,
    onSeeBooks: (LibraryMarker) -> Unit,
    onScanIsbn: (LibraryMarker) -> Unit
) {
    val viewModel: MapBottomSheetViewModel = hiltViewModel()
    val state = viewModel.uiState.collectAsState().value

    if (selected != null) {
        key(selected.id) {
            MapDetailSheet(
                marker = selected,
                currentLocation = currentLocation,
                onBack = onClearSelection,
                onSeeBooks = onSeeBooks,
                onScanIsbn = onScanIsbn,
                state = state,
                onLoadAddress = { lat, lng -> viewModel.loadAddress(lat, lng) },
                onSearchBooks = viewModel::searchBooks,
                onSaveBook = { libraryId, item, addedByUid, addedByName ->
                    viewModel.saveBook(libraryId, item, addedByUid, addedByName)
                },
                onConsumeSaveSuccess = viewModel::consumeSaveSuccess,
                onObserveComments = viewModel::observeComments,
                onCommentTextChange = viewModel::updateCommentText,
                onSubmitComment = { libraryId, uid, name ->
                    viewModel.submitComment(libraryId, uid, name)
                },
                onObserveLikes = viewModel::observeLikes,
                onToggleLike = viewModel::toggleLike
            )
        }
    } else {
        MapListSheet(
            items = items,
            currentLocation = currentLocation,
            onSelect = onSelect
        )
    }
}

@Composable
private fun MapListSheet(
    items: List<NearbyLibrary>,
    currentLocation: LatLng?,
    onSelect: (LibraryMarker) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5E7CD))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = stringResource(R.string.libraries_close_to_you),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF5D492B)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5E7CD))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_nearby_libraries),
                        color = Color(0xFF5D492B)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize()
                        .background(Color(0xFFF5E7CD))
                ) {
                    items(items) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { onSelect(item.marker) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFF5D492B), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.marker.size,
                                    color = Color(0xFFF5E7CD)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.marker.name,
                                    color = Color(0xFF3C2E1A)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            val distanceText = if (currentLocation != null) {
                                val dist = FloatArray(1)
                                android.location.Location.distanceBetween(
                                    currentLocation.latitude,
                                    currentLocation.longitude,
                                    item.marker.lat,
                                    item.marker.lng,
                                    dist
                                )
                                formatDistance(dist[0])
                            } else {
                                "—"
                            }
                            Text(
                                text = distanceText,
                                color = Color(0xFF3C2E1A)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF3C2E1A)
                            )
                        }
                        Divider(color = DividerColor)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapDetailSheet(
    marker: LibraryMarker,
    currentLocation: LatLng?,
    onBack: () -> Unit,
    onSeeBooks: (LibraryMarker) -> Unit,
    onScanIsbn: (LibraryMarker) -> Unit,
    state: MapBottomSheetUiState,
    onLoadAddress: (Double, Double) -> Unit,
    onSearchBooks: (String) -> Unit,
    onSaveBook: (String, GoogleBookItem, String?, String?) -> Unit,
    onConsumeSaveSuccess: () -> Unit,
    onObserveComments: (String) -> Unit,
    onCommentTextChange: (String) -> Unit,
    onSubmitComment: (String, String?, String?) -> Unit,
    onObserveLikes: (String, String?) -> Unit,
    onToggleLike: (String, String?) -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val showAddBookDialog = remember { mutableStateOf(false) }
    val showAddBookChoice = remember { mutableStateOf(false) }

    LaunchedEffect(marker.lat, marker.lng) {
        onLoadAddress(marker.lat, marker.lng)
    }
    LaunchedEffect(marker.id) {
        onObserveComments(marker.id)
    }
    LaunchedEffect(marker.id, currentUser?.uid) {
        onObserveLikes(marker.id, currentUser?.uid)
    }

    val distanceText = currentLocation?.let { user ->
        val dist = FloatArray(1)
        android.location.Location.distanceBetween(
            user.latitude,
            user.longitude,
            marker.lat,
            marker.lng,
            dist
        )
        formatDistance(dist[0])
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEEEEEE))
        ) {
            if (!marker.imageUrl.isNullOrBlank()) {
                val imageModel = marker.imageUrl
                SubcomposeAsyncImage(
                    model = imageModel,
                    contentDescription = marker.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                ) {
                    if (painter.state is coil.compose.AsyncImagePainter.State.Loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFCBB999)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF5D492B))
                        }
                    } else {
                        SubcomposeAsyncImageContent()
                    }
                }
            } else {
                val localImage = marker.imageLocalPath?.let { File(it) }
                if (localImage != null && localImage.exists()) {
                    SubcomposeAsyncImage(
                        model = localImage,
                        contentDescription = marker.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                    ) {
                        if (painter.state is coil.compose.AsyncImagePainter.State.Loading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFCBB999)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF5D492B))
                            }
                        } else {
                            SubcomposeAsyncImageContent()
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .background(Color(0xFFCBB999))
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = marker.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF3C2E1A),
                        modifier = Modifier.weight(1f)
                    )
                    Column(
                        horizontalAlignment = Alignment.End,
                    ) {
                        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                            IconButton(
                                modifier = Modifier.size(24.dp),
                                onClick = {onBack() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.cd_close),
                                    tint = PrimaryColor,

                                )
                            }
                        }

//                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = state.likesCount.toString(),
                                color = Color(0xFF3C2E1A),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(
                                onClick = { onToggleLike(marker.id, currentUser?.uid) },
                                modifier = Modifier.size(24.dp),
                            ) {
                                Icon(
                                    imageVector = if (state.isLiked) {
                                        Icons.Default.Favorite
                                    } else {
                                        Icons.Default.FavoriteBorder
                                    },
                                    contentDescription = stringResource(R.string.cd_like),
                                    tint = if (state.isLiked) Color(0xFFB3261E) else PrimaryColor
                                )
                            }

                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(thickness = 1.dp, color = PrimaryColor)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationCity,
                        contentDescription = null,
                        tint = Color(0xFF5D492B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.city ?: stringResource(R.string.unknown_city),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF3C2E1A)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = Color(0xFF5D492B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.street ?: stringResource(R.string.unknown_address),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF3C2E1A)
                    )
                }

                marker.createdByName?.let { creator ->
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF5D492B)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = creator,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF3C2E1A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.books_label),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF5D492B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val compact = maxWidth < 340.dp
                        if (compact) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showAddBookChoice.value = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFF4D9A5),
                                        contentColor = Color(0xFF3C2E1A)
                                    )
                                ) {
                                    Text(text = stringResource(R.string.add_book))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                }
                                Button(
                                    onClick = { onSeeBooks(marker) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD7E56B),
                                        contentColor = Color(0xFF3C2E1A)
                                    )
                                ) {
                                    Text(text = stringResource(R.string.see_books))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(imageVector = Icons.Default.MenuBook, contentDescription = null)
                                }
                            }
                        } else {
                            Row {
                                Button(
                                    onClick = { showAddBookChoice.value = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFF4D9A5),
                                        contentColor = Color(0xFF3C2E1A)
                                    )
                                ) {
                                    Text(text = stringResource(R.string.add_book))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onSeeBooks(marker) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD7E56B),
                                        contentColor = Color(0xFF3C2E1A)
                                    )
                                ) {
                                    Text(text = stringResource(R.string.see_books))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(imageVector = Icons.Default.MenuBook, contentDescription = null)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 1.dp, color = PrimaryColor)
                Spacer(modifier = Modifier.height(12.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.comments_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF5D492B)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (state.comments.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_comments),
                        color = Color(0xFF5D492B)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF7F2E8))
                            .padding(12.dp)
                    ) {
                        state.comments.forEachIndexed { index, comment ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD7E1C8)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = comment.authorName.firstOrNull()?.uppercase() ?: "?",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF3C2E1A)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = comment.authorName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF3C2E1A)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = comment.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF5D492B)
                                    )
                                }
                            }
                            if (index != state.comments.lastIndex) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Divider(color = DividerColor)
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.commentText,
                        onValueChange = onCommentTextChange,
                        modifier = Modifier.weight(1f),
                        singleLine = false,
                        label = { Text(text = stringResource(R.string.write_comment)) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            onSubmitComment(
                                marker.id,
                                currentUser?.uid,
                                currentUser?.displayName
                                    ?.takeIf { it.isNotBlank() }
                                    ?: currentUser?.email?.substringBefore("@")
                            )
                        },
                        enabled = !state.isCommentSending
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.cd_send_comment),
                            tint = if (state.isCommentSending) Color(0xFFBBAA8E) else Color(0xFF5D492B)
                        )
                    }
                }
                if (state.commentError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = state.commentError,
                        color = Color(0xFFC7190E),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

        }
    }

    if (showAddBookDialog.value) {
        AddBookDialog(
            libraryId = marker.id,
            addedByUid = currentUser?.uid,
            addedByName = currentUser?.displayName
                ?.takeIf { it.isNotBlank() }
                ?: currentUser?.email?.substringBefore("@"),
            state = state,
            onSearchBooks = onSearchBooks,
            onSaveBook = { item, uid, name ->
                onSaveBook(marker.id, item, uid, name)
            },
            onConsumeSaveSuccess = {
                onConsumeSaveSuccess()
                showAddBookDialog.value = false
            },
            onDismiss = { showAddBookDialog.value = false }
        )
    }

    if (showAddBookChoice.value) {
        AddBookChoiceDialog(
            onSearch = {
                showAddBookChoice.value = false
                showAddBookDialog.value = true
            },
            onCamera = {
                showAddBookChoice.value = false
                onScanIsbn(marker)
            },
            onDismiss = { showAddBookChoice.value = false }
        )
    }
}
