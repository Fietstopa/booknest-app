package git.pef.mendelu.cz.booknest.ui.screens.SettingsScreen.profile.core

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.navigation.INavigationRouter
import git.pef.mendelu.cz.booknest.ui.theme.OnPrimaryColor
import git.pef.mendelu.cz.booknest.ui.theme.PrimaryColor
import git.pef.mendelu.cz.booknest.ui.theme.SecondaryColor

@Composable
internal fun ChangeProfileScreen(
    navRouter: INavigationRouter
) {
    val viewModel: ChangeProfileViewModel = hiltViewModel()
    val state = viewModel.uiState.collectAsState().value
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.updatePhoto(uri)
    }
    val profileInitial = remember(state.nickname) {
        state.nickname.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    }
    val avatarModel = state.selectedPhotoUri ?: state.currentPhotoUrl

    LaunchedEffect(state.success) {
        if (state.success) {
            viewModel.consumeSuccess()
            navRouter.returnBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2E4CD))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(SecondaryColor),
                contentAlignment = Alignment.Center
            ) {
                if (avatarModel != null) {
                    AsyncImage(
                        model = avatarModel,
                        contentDescription = stringResource(R.string.cd_profile_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = profileInitial,
                        color = PrimaryColor
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { photoPicker.launch("image/*") },
                enabled = !state.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor,
                    contentColor = OnPrimaryColor
                )
            ) {
                Text(text = stringResource(R.string.change_photo))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = state.nickname,
            onValueChange = viewModel::updateNickname,
            label = { Text(text = stringResource(R.string.nickname_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (state.error != null) {
            Text(
                text = state.error,
                color = Color(0xFFB3261E)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        Button(
            onClick = viewModel::saveProfile,
            enabled = !state.isSaving,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryColor,
                contentColor = OnPrimaryColor
            )
        ) {
            Text(text = stringResource(R.string.save))
        }
    }
}
