package com.sc.eventnotifyke.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sc.eventnotifyke.ui.screens.*


@Composable
fun NavGraph(navController: NavHostController) {

    // NavHost defines the navigation graph
    // startDestination is the first screen shown
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        // splash screen
        composable(route = Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        // login screen
        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        // register screen
        composable(route = Screen.Register.route) {
            RegisterScreen(navController = navController)
        }

        // forgot password screen
        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }

        // home screen
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        // event detail screen
        composable(route = Screen.EventDetail.route) {
            EventDetailScreen(navController = navController)
        }

        // post event screen
        composable(route = Screen.PostEvent.route) {
            PostEventScreen(navController = navController)
        }

        // reminders screen
        composable(route = Screen.Reminders.route) {
            ReminderScreen(navController = navController)
        }

        // profile screen
        composable(route = Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

    }
}