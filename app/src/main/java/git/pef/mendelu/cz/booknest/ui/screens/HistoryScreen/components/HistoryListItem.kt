package git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.models.BookHistoryItem
import git.pef.mendelu.cz.booknest.ui.screens.HistoryScreen.utils.formatHistoryDate

@Composable
internal fun HistoryListItem(
    item: BookHistoryItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF2E4CD))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF3C2E1A)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.libraryName,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B5A40)
            )
        }
        Text(
            text = formatHistoryDate(
                item.addedAtMillis,
                stringResource(R.string.unknown_date)
            ),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF3C2E1A)
        )
    }
}
