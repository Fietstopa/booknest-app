package git.pef.mendelu.cz.booknest.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import dagger.hilt.android.AndroidEntryPoint
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.ui.activities.MainActivity
import git.pef.mendelu.cz.booknest.ui.theme.BooknestTheme
import git.pef.mendelu.cz.booknest.ui.theme.PrimaryColor
import git.pef.mendelu.cz.booknest.ui.theme.SecondaryColor
import git.pef.mendelu.cz.booknest.ui.theme.OnPrimaryColor

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, RegisterActivity::class.java)
        }
    }

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BooknestTheme {
                RegisterScreen(
                    viewModel = viewModel,
                    onRegister = {
                        viewModel.registerWithEmail {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    },
                    onBackToLogin = { finish() }
                )
            }
        }
    }
}

@Composable
private fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegister: () -> Unit,
    onBackToLogin: () -> Unit
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
            text = stringResource(R.string.register_title),
            fontFamily = poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            color = SecondaryColor
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = state.nickname,
            onValueChange = viewModel::updateNickname,
            label = { Text(stringResource(R.string.nickname_label)) },
            modifier = Modifier.fillMaxWidth(),
            colors = inputColors
        )
        Spacer(modifier = Modifier.height(8.dp))
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
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = viewModel::updateConfirmPassword,
            label = { Text(stringResource(R.string.confirm_password_label)) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            colors = inputColors
        )
        if (!state.errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.errorMessage ?: "",
                color = SecondaryColor,
                fontFamily = poppins,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = SecondaryColor,
                contentColor = PrimaryColor
            )
        ) {
            Text(text = stringResource(R.string.register_button), fontFamily = poppins)
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onBackToLogin) {
            Text(text = stringResource(R.string.back_to_login), fontFamily = poppins, color = SecondaryColor)
        }
    }
}
