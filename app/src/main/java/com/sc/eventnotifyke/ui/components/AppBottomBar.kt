package com.sc.eventnotifyke.ui.components



import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun AppBottomBar(
    currentRoute: String?,
    onHomeClick: () -> Unit,
    onBrowseClick: () -> Unit,
    onMyEventsClick: () -> Unit,
    onProfileClick: () -> Unit
) {

    // host - navigation bar
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {

        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = onHomeClick,
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentRoute == "browse",
            onClick = onBrowseClick,
            icon = { Icon(Icons.Default.Search, contentDescription = "Browse") },
            label = { Text("Browse") }
        )

        NavigationBarItem(
            selected = currentRoute == "my_events",
            onClick = onMyEventsClick,
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "My Events") },
            label = { Text("My Events") }
        )

        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = onProfileClick,
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}