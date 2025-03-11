package com.example.bowls

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.core.view.WindowCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use modern WindowInsetsController to hide system UI
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).let { controller ->
            controller.hide(androidx.core.view.WindowInsetsCompat.Type.statusBars())
            controller.hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Set content immediately to override launcher
        setContentView(ComposeView(this).apply {
            setContent {
                SplashScreen()
            }
        })

        // Delay and transition to MainActivity
        CoroutineScope(Dispatchers.Main).launch {
            delay(1500) // Revert to 1.5 seconds to match first launch
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
}

@Composable
fun SplashScreen() {
    Image(
        painter = painterResource(id = R.drawable.splash_background),
        contentDescription = "Splash Screen",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds
    )
}