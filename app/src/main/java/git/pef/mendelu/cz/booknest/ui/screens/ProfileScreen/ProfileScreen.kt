package git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.navigation.Destination
import git.pef.mendelu.cz.booknest.navigation.INavigationRouter
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel
import git.pef.mendelu.cz.booknest.ui.theme.SecondaryColor

@Composable
fun ProfileScreen(navRouter: INavigationRouter) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val state = viewModel.uiState.collectAsState().value

    val background = Color(0xFF5D492B)
    val accent = Color(0xFFDDEB7A)
    val surface = Color(0xFFF2EDE3)
    val textPrimary = Color(0xFFF5EBDD)
    val titleFont = FontFamily(Font(R.font.poppins_medium))
    val photoUrl = state.photoUrl

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(26.dp))
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.onboarding_left),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(26.dp))
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = state.nickname,
                color = textPrimary,
                fontFamily = titleFont,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(accent)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp, vertical =  24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBlock(
                    value = state.addedBooksCount.toString(),
                    label = stringResource(R.string.books_added_label)
                )
                StatBlock(
                    value = state.addedLibrariesCount.toString(),
                    label = stringResource(R.string.libraries_added_label)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(surface)
                    .padding(20.dp)
            ) {
                SectionHeader(
                    title = stringResource(R.string.saved_books_title),
                    textColor = Color(0xFF5D492B),
                    fontFamily = titleFont
                )
                if (state.savedBooks.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_saved_books),
                        color = Color(0xFF5D492B)
                    )
                } else {
                    val previewBooks = state.savedBooks.take(3)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        previewBooks.forEach { book ->
                            Column(
                                modifier = Modifier
                                    .width(90.dp)
                                    .clickable { navRouter.navigateToBookDetail(book.id) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 90.dp, height = 120.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SecondaryColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (book.thumbnail.isNullOrBlank()) {
                                        Image(
                                            painter = painterResource(R.drawable.books),
                                            contentDescription = stringResource(R.string.no_cover),
                                            modifier = Modifier.size(64.dp)
                                        )
                                    } else {
                                        AsyncImage(
                                            model = book.thumbnail,
                                            contentDescription = book.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.matchParentSize()
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = book.title,
                                    color = Color(0xFF5D492B),
                                    fontSize = 12.sp,
                                    maxLines = 2
                                )
                            }
                        }
                        if (state.savedBooks.size > 3) {
                            Column(
                                modifier = Modifier
                                    .width(90.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 90.dp, height = 120.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFE9DFC8))
                                        .clickable { navRouter.navigateToSavedBooks() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+",
                                        color = Color(0xFF5D492B),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.see_more_tile),
                                    color = Color(0xFF5D492B),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                SectionHeader(
                    title = stringResource(R.string.added_books_title),
                    textColor = Color(0xFF5D492B),
                    fontFamily = titleFont
                )
                if (state.addedBooks.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_added_books),
                        color = Color(0xFF5D492B)
                    )
                } else {
                    val previewAddedBooks = state.addedBooks.take(3)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        previewAddedBooks.forEach { book ->
                            Column(
                                modifier = Modifier
                                    .width(90.dp)
                                    .clickable { navRouter.navigateToBookDetail(book.id) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 90.dp, height = 120.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SecondaryColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (book.thumbnail.isNullOrBlank()) {
                                        Image(
                                            painter = painterResource(R.drawable.books),
                                            contentDescription = stringResource(R.string.no_cover),
                                            modifier = Modifier.size(64.dp)
                                        )
                                    } else {
                                        AsyncImage(
                                            model = book.thumbnail,
                                            contentDescription = book.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.matchParentSize()
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = book.title,
                                    color = Color(0xFF5D492B),
                                    fontSize = 12.sp,
                                    maxLines = 2
                                )
                            }
                        }
                        if (state.addedBooks.size > 3) {
                            Column(
                                modifier = Modifier
                                    .width(90.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 90.dp, height = 120.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFE9DFC8))
                                        .clickable { navRouter.navigateToAddedBooks() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+",
                                        color = Color(0xFF5D492B),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.see_more_tile),
                                    color = Color(0xFF5D492B),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                SectionHeader(
                    title = stringResource(R.string.added_libraries_title),
                    textColor = Color(0xFF5D492B),
                    fontFamily = titleFont
                )
                if (state.addedLibraries.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_added_libraries),
                        color = Color(0xFF5D492B)
                    )
                } else {
                    val previewLibraries = state.addedLibraries.take(3)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        previewLibraries.forEach { library ->
                            Column(
                                modifier = Modifier
                                    .width(150.dp)
                                    .clickable {
                                        val navController = navRouter.getNavController()
                                        navController.currentBackStackEntry
                                            ?.savedStateHandle
                                            ?.set("focusLibraryId", library.id)
                                        navController.currentBackStackEntry
                                            ?.savedStateHandle
                                            ?.set("focusLibraryBounce", true)
                                        navRouter.navigateToMap()
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 150.dp, height = 110.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF2B241C)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (library.imageUrl.isNullOrBlank()) {
                                        Text(
                                            text = stringResource(R.string.no_image),
                                            color = Color(0xFFF5E7CD),
                                            fontSize = 10.sp
                                        )
                                    } else {
                                        AsyncImage(
                                            model = library.imageUrl,
                                            contentDescription = library.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.matchParentSize()
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = library.name,
                                    color = Color(0xFF5D492B),
                                    fontSize = 12.sp,
                                    maxLines = 2
                                )
                            }
                        }
                        if (state.addedLibraries.size > 3) {
                            Column(
                                modifier = Modifier
                                    .width(150.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 150.dp, height = 110.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFE9DFC8))
                                        .clickable { navRouter.navigateToAddedLibraries() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+",
                                        color = Color(0xFF5D492B),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.see_more_tile),
                                    color = Color(0xFF5D492B),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

    }
}

@Composable
private fun StatBlock(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.wrapContentWidth(Alignment.CenterHorizontally),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = Color(0xFF3C2E1A),
            fontFamily = FontFamily(Font(R.font.poppins_medium)),
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            color = Color(0xFF3C2E1A),
            fontFamily = FontFamily(Font(R.font.poppins_light)),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    textColor: Color,
    fontFamily: FontFamily
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = textColor,
            fontFamily = fontFamily,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(textColor.copy(alpha = 0.6f))
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}
