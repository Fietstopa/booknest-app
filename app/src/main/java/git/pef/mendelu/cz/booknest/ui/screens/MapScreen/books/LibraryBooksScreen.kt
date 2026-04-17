package git.pef.mendelu.cz.booknest.ui.screens.MapScreen.books

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import git.pef.mendelu.cz.booknest.navigation.INavigationRouter
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import git.pef.mendelu.cz.booknest.ui.screens.MapScreen.models.LibraryBook
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.ui.theme.SecondaryColor

@Composable
internal fun LibraryBooksScreen(
    libraryId: String,
    navRouter: INavigationRouter
) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    val books = remember { mutableStateListOf<LibraryBook>() }

    DisposableEffect(libraryId) {
        val registration = firestore.collection("libraries")
            .document(libraryId)
            .collection("books")
            .addSnapshotListener { snapshot, error ->
                if (snapshot == null || error != null) {
                    return@addSnapshotListener
                }
                val updated = snapshot.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val authors = doc.get("authors") as? List<String> ?: emptyList()
                    val thumbnail = doc.getString("thumbnail")
                    LibraryBook(doc.id, title, authors, thumbnail)
                }
                books.clear()
                books.addAll(updated)
            }
        onDispose { registration.remove() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F1B16))
    ) {
        if (books.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.library),
                        contentDescription = stringResource(R.string.no_books_yet),
                        modifier = Modifier.size(200.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.no_books_yet),
                        color = Color(0xFFF5E7CD)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(books) { book ->
                    Column(
                        modifier = Modifier.clickable { navRouter.navigateToBookDetail(book.id) }
                    ) {
                        if (book.thumbnail.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SecondaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.books),
                                    contentDescription = stringResource(R.string.no_cover),
                                    modifier = Modifier.size(96.dp)
                                )
                            }
                        } else {
                            AsyncImage(
                                model = book.thumbnail,
                                contentDescription = book.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2B241C))
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF5E7CD),
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
