package git.pef.mendelu.cz.booknest.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dagger.hilt.android.AndroidEntryPoint
import git.pef.mendelu.cz.booknest.R
import git.pef.mendelu.cz.booknest.ui.auth.LoginActivity
import git.pef.mendelu.cz.booknest.ui.theme.BooknestTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AppIntroActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, AppIntroActivity::class.java)
        }
    }

    private val viewModel: AppIntroViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BooknestTheme {
                OnboardingScreen(
                    onContinue = { continueToMainActivity() }
                )
            }
        }
    }

    private fun continueToMainActivity() {
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.setFirstRunComplete()
            val hasUser = FirebaseAuth.getInstance().currentUser != null
            if (hasUser) {
                startActivity(Intent(this@AppIntroActivity, MainActivity::class.java))
            } else {
                startActivity(LoginActivity.createIntent(this@AppIntroActivity))
            }
            finish()
        }
    }
}

@Composable
private fun OnboardingScreen(onContinue: () -> Unit) {
    val accent = Color(0xFFCBB999)
    val textColor = Color(0xFFE2D3BD)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF5D492B))
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = stringResource(R.string.cd_logo),
                modifier = Modifier.size(196.dp)
            )


        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(rotationZ = -10f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF2E6EBB))
                ) {
                    Image(
                        painter = painterResource(R.drawable.onboarding_right),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Chip(
                    text = stringResource(R.string.intro_chip_mark_libraries),

                    background = Color(0xFFD6C3A1),
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))

                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .offset(x = 24.dp)
            ) {Chip(
                text = stringResource(R.string.intro_chip_help_community),
                background = Color(0xFFD6C3A1),
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))

            )

                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF2E6EBB))
                ) {
                    Image(
                        painter = painterResource(R.drawable.onboarding_left),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Column {
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.intro_title_prefix))
                    withStyle(SpanStyle(color = Color(0xFFE0EE6A))) {
                        append(stringResource(R.string.intro_title_highlight))
                    }
                    append("\n")
                    append(stringResource(R.string.intro_title_suffix))
                },
                color = textColor,
                fontFamily = FontFamily(Font(R.font.poppins_light)),
                fontWeight = FontWeight.Light,
                fontSize = 32.sp,
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDDEB7A),
                    contentColor = Color(0xFF5D492B)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End),
                shape = RoundedCornerShape(22.dp)
            ) {
                Text(
                    text = stringResource(R.string.intro_lets_go),
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.poppins_medium))
                )
            }
        }
    }
}

@Composable
private fun Chip(
    text: String,
    background: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFF3C2E1A),
            fontFamily = FontFamily(Font(R.font.poppins_medium)),
            fontSize = 12.sp
        )
    }
}
