package com.sc.eventnotifyke.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.sc.eventnotifyke.navigation.Screen
import com.sc.eventnotifyke.viewmodel.AuthViewModel
import com.sc.eventnotifyke.viewmodel.AuthState
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {

    // Custom Luxury Palette derived from your hex preference
    val baseCanvasColor = Color(0xFF0F1818)    // 👈 Your exact color choice
    val brandTealAccent = Color(0xFF00B39F)    // High-contrast neon teal accent

    // Animating the logo scale with a bouncy spring effect
    val scale = remember { Animatable(initialValue = 0f) }

    // Collect the authentication state reactively from the shared ViewModel
    val authState by authViewModel.authState.collectAsState()

    // LaunchedEffect to control splash screen duration and handle reactive routing
    LaunchedEffect(key1 = authState) {
        // Run the visual entrance animation instantly on component launch
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        // Hold the splash screen visibility for a brief aesthetic duration (2 seconds)
        delay(timeMillis = 2000L)

        // Evaluate user auth configuration right from the background flow
        when (authState) {
            is AuthState.Success -> {
                // Persistent user session detected, hop right over the auth checkpoint
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
            is AuthState.Loading -> {
                // If Firebase is still resolving the initial session loop, wait for completion
                return@LaunchedEffect
            }
            else -> {
                // No active user detected (Idle, Error, or Logout), route cleanly to sign in form
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
    }

    // ── UI ───────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Using the premium vertical gradient to match the Login Screen canvas exactly
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF070B0B), baseCanvasColor)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // App icon with spring scale animation styled in neon teal
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = "EventNotify KE Logo",
                tint = brandTealAccent, // 👈 Shifted to your premium interactive color
                modifier = Modifier
                    .size(96.dp)
                    .scale(scale.value) // Apply spring animation
            )

            Spacer(modifier = Modifier.height(20.dp))

            // App name with the unified typography and matching accents
            Row {
                Text(
                    text = "EventNotify",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = " KE",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = brandTealAccent // 👈 Changed from harsh red to glowing premium teal
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline using your clean system styles
            Text(
                text = "Never Miss Local Events in Your Estate",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f) // Soft opacity matches Login Screen
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