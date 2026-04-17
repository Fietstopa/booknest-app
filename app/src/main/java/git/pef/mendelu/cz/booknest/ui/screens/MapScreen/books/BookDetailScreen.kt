package git.pef.mendelu.cz.booknest.ui.screens.MapScreen.books

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.communication.BookApiProvider
import git.pef.mendelu.cz.booknest.communication.BookRemoteRepositoryImpl
import git.pef.mendelu.cz.booknest.communication.CommunicationResult
import git.pef.mendelu.cz.booknest.communication.GoogleBookItem
import git.pef.mendelu.cz.booknest.navigation.INavigationRouter
import git.pef.mendelu.cz.booknest.ui.theme.SecondaryColor

@Composable
internal fun BookDetailScreen(
    navRouter: INavigationRouter,
    bookId: String
) {
    val booksRepository = remember { BookRemoteRepositoryImpl(BookApiProvider.create()) }
    val apiBook = remember { mutableStateOf<GoogleBookItem?>(null) }
    val error = remember { mutableStateOf<String?>(null) }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isSaved = remember { mutableStateOf(false) }
    val loadFailedMessage = stringResource(R.string.error_failed_to_load_book)
    val connectionErrorMessage = stringResource(R.string.error_connection)
    val unexpectedErrorMessage = stringResource(R.string.error_unexpected)
    val bookDetailTitle = stringResource(R.string.book_detail_title)

    LaunchedEffect(bookId) {
        when (val response = booksRepository.getBookById(bookId)) {
            is CommunicationResult.Success -> {
                apiBook.value = response.data
                error.value = null
            }
            is CommunicationResult.Error -> {
                error.value = response.error.message ?: loadFailedMessage
            }
            is CommunicationResult.ConnectionError -> {
                error.value = connectionErrorMessage
            }
            is CommunicationResult.Exception -> {
                error.value = response.exception.message ?: unexpectedErrorMessage
            }
        }
    }

    val info = apiBook.value?.volumeInfo
    val title = info?.title ?: bookDetailTitle
    val subtitle = info?.subtitle
    val authors = info?.authors ?: emptyList()
    val description = info?.description?.let { stripHtml(it) }
    val thumbnail = info?.imageLinks?.thumbnail?.replace("http://", "https://")

    DisposableEffect(bookId, currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid.isNullOrBlank()) {
            isSaved.value = false
            onDispose { }
        } else {
            val docRef = firestore.collection("users")
                .document(uid)
                .collection("savedBooks")
                .document(bookId)
            val registration = docRef.addSnapshotListener { snapshot, _ ->
                isSaved.value = snapshot?.exists() == true
            }
            onDispose { registration.remove() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF5D492B))
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
        ) {
            if (!thumbnail.isNullOrBlank()) {
                AsyncImage(
                    model = thumbnail,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(24.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x88000000))
                )
                AsyncImage(
                    model = thumbnail,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 190.dp, height = 260.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2B241C))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SecondaryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.books),
                        contentDescription = stringResource(R.string.no_cover),
                        modifier = Modifier.size(120.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFF5E7CD),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        val uid = currentUser?.uid ?: return@IconButton
                        val docRef = firestore.collection("users")
                            .document(uid)
                            .collection("savedBooks")
                            .document(bookId)
                        if (isSaved.value) {
                            docRef.delete()
                        } else {
                            val bookData = mapOf(
                                "volumeId" to bookId,
                                "title" to title,
                                "authors" to authors,
                                "thumbnail" to thumbnail,
                                "savedAt" to FieldValue.serverTimestamp()
                            )
                            docRef.set(bookData)
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isSaved.value) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Default.FavoriteBorder
                        },
                        contentDescription = stringResource(R.string.cd_save_book),
                        tint = if (isSaved.value) Color(0xFFDDEB7A) else Color(0xFFF5E7CD)
                    )
                }
            }
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFF5E7CD)
                )
            }
            if (authors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = authors.joinToString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFE9DFC8)
                )
            }
            if (!description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFF5E7CD)
                )
            }
            error.value?.let { message ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFFC2C2)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun stripHtml(text: String): String {
    return text.replace(Regex("<[^>]*>"), "").replace("&nbsp;", " ").trim()
}
