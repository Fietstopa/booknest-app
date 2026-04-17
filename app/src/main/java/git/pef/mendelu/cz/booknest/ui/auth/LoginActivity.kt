package git.pef.mendelu.cz.booknest.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.ui.activities.MainActivity
import git.pef.mendelu.cz.booknest.ui.theme.BooknestTheme
import git.pef.mendelu.cz.booknest.ui.theme.OnPrimaryColor
import git.pef.mendelu.cz.booknest.ui.theme.PrimaryColor
import git.pef.mendelu.cz.booknest.ui.theme.SecondaryColor

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webClientId = getString(R.string.default_web_client_id)
        val googleSignInEnabled = webClientId.isNotBlank() && webClientId != "REPLACE_WITH_WEB_CLIENT_ID"
        val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
        if (googleSignInEnabled) {
            gsoBuilder.requestIdToken(webClientId)
        }
        val gso = gsoBuilder.build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    viewModel.signInWithGoogle(idToken) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                } else {
                    viewModel.setError(getString(R.string.error_google_sign_in_failed))
                }
            } catch (error: ApiException) {
                viewModel.setError(
                    error.localizedMessage ?: getString(R.string.error_google_sign_in_failed)
                )
            }
        }

        setContent {
            BooknestTheme {
                LoginScreen(
                    viewModel = viewModel,
                    onLogin = {
                        viewModel.signInWithEmail {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    },
                    onGoogleLogin = {
                        if (googleSignInEnabled) {
                            launcher.launch(googleSignInClient.signInIntent)
                        } else {
                            viewModel.setError(getString(R.string.error_google_sign_in_not_configured))
                        }
                    },
                    googleEnabled = googleSignInEnabled,
                    onRegister = {
                        startActivity(RegisterActivity.createIntent(this))
                    }
                )
            }
        }
    }
}

@Composable
private fun LoginScreen(
    viewModel: AuthViewModel,
    onLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
    googleEnabled: Boolean,
    onRegister: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val poppins = FontFamily(
        Font(R.font.poppins_light, FontWeight.Light),
        Font(R.font.poppins_medium, FontWeight.Medium),
        Font(R.font.poppins_semibold, FontWeight.SemiBold),
        Font(R.font.poppins_bold, FontWeight.Bold)
    )
    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = SecondaryColor,
        unfocusedBorderColor = SecondaryColor.copy(alpha = 0.7f),
        focusedLabelColor = SecondaryColor,
        unfocusedLabelColor = SecondaryColor.copy(alpha = 0.85f),
        focusedPlaceholderColor = SecondaryColor.copy(alpha = 0.85f),
        unfocusedPlaceholderColor = SecondaryColor.copy(alpha = 0.85f),
        focusedTextColor = OnPrimaryColor,
        unfocusedTextColor = OnPrimaryColor,
        cursorColor = OnPrimaryColor
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryColor)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.login_title),
            fontFamily = poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            color = SecondaryColor
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::updateEmail,
            label = { Text(stringResource(R.string.email_label)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = inputColors
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::updatePassword,
            label = { Text(stringResource(R.string.password_label)) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            colors = inputColors
        )
        if (!state.errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.errorMessage ?: "",
                color = Color(0xFFFFC1C1),
                fontFamily = poppins,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = SecondaryColor,
                contentColor = PrimaryColor
            )
        ) {
            Text(text = stringResource(R.string.login_button), fontFamily = poppins)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onGoogleLogin,
            modifier = Modifier.fillMaxWidth(),
            enabled = googleEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = SecondaryColor,
                contentColor = PrimaryColor
            )
        ) {
            Text(text = stringResource(R.string.continue_with_google), fontFamily = poppins)
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onRegister) {
            Text(text = stringResource(R.string.create_account), fontFamily = poppins, color = SecondaryColor)
        }
    }
}
