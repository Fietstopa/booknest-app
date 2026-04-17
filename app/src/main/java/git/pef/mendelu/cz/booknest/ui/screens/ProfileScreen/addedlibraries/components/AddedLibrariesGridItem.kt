package git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.addedlibraries.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.ui.screens.ProfileScreen.addedlibraries.models.AddedLibraryItem

@Composable
internal fun AddedLibrariesGridItem(
    item: AddedLibraryItem,
    onClick: (AddedLibraryItem) -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick(item) }
    ) {
        if (item.imageUrl.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2B241C)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_image),
                    color = Color(0xFFF5E7CD),
                    fontSize = 10.sp
                )
            }
        } else {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.name,
            color = Color(0xFFF5E7CD),
            fontSize = 12.sp,
            maxLines = 2
        )
    }
}
