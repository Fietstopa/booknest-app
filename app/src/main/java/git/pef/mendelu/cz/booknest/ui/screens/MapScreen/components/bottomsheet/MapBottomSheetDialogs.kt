package git.pef.mendelu.cz.booknest.ui.screens.MapScreen.components.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import git.pef.mendelu.cz.booknest.communication.GoogleBookItem
import git.pef.mendelu.cz.booknest.R

@Composable
internal fun AddBookChoiceDialog(
    onSearch: () -> Unit,
    onCamera: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.add_book_title)) },
        text = {
            Column {
                Button(
                    onClick = onCamera,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.scan_isbn))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onSearch,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.search_by_name))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.close))
            }
        }
    )
}

@Composable
internal fun AddBookDialog(
    libraryId: String,
    addedByUid: String?,
    addedByName: String?,
    state: MapBottomSheetUiState,
    onSearchBooks: (String) -> Unit,
    onSaveBook: (GoogleBookItem, String?, String?) -> Unit,
    onConsumeSaveSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    val query = remember { mutableStateOf("") }
    val results = state.searchResults
    val isLoading = state.isSearching
    val error = state.searchError ?: state.saveError

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            onConsumeSaveSuccess()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.add_book_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = query.value,
                    onValueChange = { query.value = it },
                    label = { Text(text = stringResource(R.string.search_by_title_or_author)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val searchText = query.value.trim()
                        onSearchBooks(searchText)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.search))
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                error?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = message, color = Color(0xFFB3261E))
                }

                if (results.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        itemsIndexed(results) { index, item ->
                            val info = item.volumeInfo
                            val thumbnail = info.imageLinks?.thumbnail?.replace("http://", "https://")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSaveBook(item, addedByUid, addedByName)
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (thumbnail.isNullOrBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF2B241C)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.not_available_short),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFF5E7CD)
                                        )
                                    }
                                } else {
                                    AsyncImage(
                                        model = thumbnail,
                                        contentDescription = info.title,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = info.title ?: stringResource(R.string.unknown_title),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF3C2E1A)
                                    )
                                    if (!info.authors.isNullOrEmpty()) {
                                        Text(
                                            text = info.authors.joinToString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF5D492B)
                                        )
                                    }
                                }
                            }
                            if (index != results.lastIndex) {
                                Divider(color = Color(0xFF5D492B).copy(alpha = 0.2f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.close))
            }
        }
    )
}
