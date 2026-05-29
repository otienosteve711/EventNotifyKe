package com.sc.eventnotifyke.navigation


// defines all screen routes as sealed classes
// sealed class ensures type safety for navigation
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object EventDetail : Screen("event_detail")
    object PostEvent : Screen("post_event")

    object Profile : Screen("profile")
    object Browse : Screen("browse")
    object MyEvents : Screen("my_events")
}