package com.caraid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.caraid.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.initialize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreenActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreen {
                // Launch Firebase in a background thread
                lifecycleScope.launch {
                    Firebase.initialize(this@SplashScreenActivity)

                    // Start the next activity after initialization
                    startActivity(Intent(this@SplashScreenActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycleScope.launchWhenCreated {
            delay(2000) // Delay for 2 seconds
            onTimeout()
        }
    }
    // Rest of your SplashScreen composable content...
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CaraidPurplePrimary),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Add your logo or any other visual elements here
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with your app's icon
            contentDescription = "App Logo",
            tint = Color.White,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Caraid",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}