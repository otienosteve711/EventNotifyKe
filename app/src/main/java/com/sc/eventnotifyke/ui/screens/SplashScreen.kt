package com.sc.eventnotifyke.ui.screens


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.sc.eventnotifyke.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {

    // animating the logo scale with a bouncy spring effect
    val scale = remember { Animatable(initialValue = 0f) }

    // launched effect to control splash screen duration
    LaunchedEffect(key1 = Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        delay(timeMillis = 2000L) // splash screen lasts 2 seconds
        // navigate to log in after delay
        navController.navigate(Screen.Login.route) {
            // remove splash from back stack
            // user cannot navigate back to splash
            popUpTo(Screen.Splash.route) {
                inclusive = true
            }
        }
    }

    // ── UI ───────────────────────────────────────
    // Box as the main container centered on screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFF1B5E20)), // deep green background
        contentAlignment = Alignment.Center
    ) {
        // column for vertical arrangement of elements
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // app icon with spring scale animation
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = "EventNotify KE Logo",
                tint = Color.White,
                modifier = Modifier
                    .size(96.dp)
                    .scale(scale.value) // apply spring animation
            )

            Spacer(modifier = Modifier.height(16.dp))

            // app name
            Row {
                Text(
                    text = "EventNotify",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = " KE",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFD32F2F) // red accent for KE
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // tagline
            Text(
                text = "Never Miss Local Events in Your Estate",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

// ── PREVIEW ──────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    MaterialTheme {
        SplashScreen(navController = rememberNavController())
    }
}