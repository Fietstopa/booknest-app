package git.pef.mendelu.cz.booknest.ui.screens.SettingsScreen.core

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.navigation.INavigationRouter

@Composable
internal fun SettingsScreen(
    navRouter: INavigationRouter
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2E4CD))
            .padding(20.dp)
    ) {
        SettingsRow(
            title = stringResource(R.string.change_profile_title),
            onClick = navRouter::navigateToChangeProfile
        )
        Spacer(modifier = Modifier.height(12.dp))
        SettingsRow(
            title = stringResource(R.string.settings_language),
            onClick = navRouter::navigateToSettingsLanguage
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color(0xFF3C2E1A),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF3C2E1A)
        )
    }
}
