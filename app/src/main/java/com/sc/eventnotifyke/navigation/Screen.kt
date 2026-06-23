package com.sc.eventnotifyke.navigation

// defines all screen routes as sealed classes
// sealed class ensures type safety for navigation
sealed class Screen(val route: String) {
    object Splash        : Screen("splash")
    object Login         : Screen("login")
    object Register      : Screen("register")
    object ForgotPassword: Screen("forgot_password")
    object Home          : Screen("home")
    object PostEvent     : Screen("post_event")
    object Profile       : Screen("profile")
    object Browse        : Screen("browse")
    object MyEvents      : Screen("my_events")

    // routes with arguments use a template for the composable definition ...
    object EventDetail   : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }

    // ... and a separate createRoute() for navigating to them
    object EditEvent     : Screen("edit_event/{eventId}") {
        fun createRoute(eventId: String) = "edit_event/$eventId"
    }
}