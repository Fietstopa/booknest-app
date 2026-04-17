package git.pef.mendelu.cz.booknest.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.ui.auth.LoginActivity
import git.pef.mendelu.cz.booknest.ui.theme.BooknestTheme
import git.pef.mendelu.cz.booknest.ui.theme.PrimaryColor

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private val viewModel: SplashScreenActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BooknestTheme {
                val state by viewModel.uiState.collectAsState()

                LaunchedEffect(state.runForFirstTime, state.continueToApp) {
                    when {
                        state.runForFirstTime -> {
                            startActivity(AppIntroActivity.createIntent(this@SplashActivity))
                            finish()
                        }
                        state.continueToApp -> {
                            val hasUser = FirebaseAuth.getInstance().currentUser != null
                            if (hasUser) {
                                startActivity(MainActivity.createIntent(this@SplashActivity))
                            } else {
                                startActivity(LoginActivity.createIntent(this@SplashActivity))
                            }
                            finish()
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PrimaryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.applogo),
                        contentDescription = stringResource(R.string.cd_app_logo),
                        modifier = Modifier.size(180.dp)
                    )
                }
            }
        }
    }
}
