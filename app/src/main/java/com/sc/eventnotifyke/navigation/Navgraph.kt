package com.sc.eventnotifyke.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sc.eventnotifyke.ui.screens.*
import com.sc.eventnotifyke.viewmodel.AuthViewModel
import com.sc.eventnotifyke.viewmodel.EventViewModel


@Composable
fun NavGraph(navController: NavHostController) {

    // ── Shared ViewModels — single instance across all screens ───────────────
    val authViewModel: AuthViewModel = viewModel()
    val eventViewModel: EventViewModel = viewModel()

    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route
    ) {

        composable(route = Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(navController = navController, authViewModel = authViewModel)
        }

        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController, authViewModel = authViewModel)
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                navController  = navController,
                authViewModel  = authViewModel,
                eventViewModel = eventViewModel
            )
        }

        composable(route = Screen.EventDetail.route) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailScreen(
                navController  = navController,
                eventId        = eventId,
                eventViewModel = eventViewModel,
                authViewModel  = authViewModel
            )
        }

        composable(route = Screen.PostEvent.route) {
            PostEventScreen(
                navController  = navController,
                authViewModel  = authViewModel,
                eventViewModel = eventViewModel
            )
        }

        // EditEvent — reuses PostEventScreen, passing eventId as argument
        composable(route = Screen.EditEvent.route) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            PostEventScreen(
                navController  = navController,
                authViewModel  = authViewModel,
                eventViewModel = eventViewModel,
                eventId        = eventId         // PostEventScreen uses this to load for edit
            )
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(navController = navController, authViewModel = authViewModel)
        }

        composable(route = Screen.Browse.route) {
            BrowseScreen(navController = navController, eventViewModel = eventViewModel)
        }

        composable(route = Screen.MyEvents.route) {
            MyEventsScreen(
                navController  = navController,
                eventViewModel = eventViewModel,
                authViewModel  = authViewModel   // added
            )
        }
    }
}