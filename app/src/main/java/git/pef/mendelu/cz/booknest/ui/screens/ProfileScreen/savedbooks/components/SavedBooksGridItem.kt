package git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.savedbooks.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.savedbooks.models.SavedBookItem
import git.pef.mendelu.cz.booknest.ui.theme.SecondaryColor

@Composable
internal fun SavedBooksGridItem(
    item: SavedBookItem,
    onClick: (SavedBookItem) -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick(item) }
    ) {
        if (item.thumbnail.isNullOrBlank()) {
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
                model = item.thumbnail,
                contentDescription = item.title,
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
            text = item.title,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFF5E7CD),
            maxLines = 2
        )
    }
}
