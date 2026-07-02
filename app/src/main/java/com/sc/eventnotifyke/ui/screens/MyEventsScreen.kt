package com.sc.eventnotifyke.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.Timestamp
import com.sc.eventnotifyke.models.EventItem
import com.sc.eventnotifyke.models.EventStatus
import com.sc.eventnotifyke.navigation.Screen
import com.sc.eventnotifyke.ui.components.AppBottomBar
import com.sc.eventnotifyke.viewmodel.AuthViewModel
import com.sc.eventnotifyke.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyEventsScreen(
    navController: NavController,
    eventViewModel: EventViewModel,
    authViewModel: AuthViewModel
) {
    val profile        by authViewModel.currentProfile.collectAsState()
    val userId         = profile?.uid ?: ""
    val allEvents      by eventViewModel.allEvents.collectAsState()
    val bookmarkedIds  by eventViewModel.bookmarkedEventIds.collectAsState()

    // Only show bookmarked events that are still upcoming.
    // For postponed events, `date` already holds the new date, so this
    // naturally keeps them visible as long as the new date hasn't passed.
    val now = Timestamp.now()
    val savedEvents = allEvents.filter {
        it.id in bookmarkedIds && it.date >= now
    }

    // Real-time bookmark listener — attach once userId is available
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            eventViewModel.loadBookmarks(userId)
        }
    }

    // Re-fetch all events every time this screen resumes (e.g. after bookmarking
    // a brand-new event on HomeScreen and navigating back here)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            eventViewModel.loadAllEvents()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("My Events", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            AppBottomBar(
                currentRoute    = Screen.MyEvents.route,
                onHomeClick     = { navController.navigate(Screen.Home.route) },
                onBrowseClick   = { navController.navigate(Screen.Browse.route) },
                onMyEventsClick = {},
                onProfileClick  = { navController.navigate(Screen.Profile.route) }
            )
        }
    ) { innerPadding ->

        if (savedEvents.isEmpty()) {
            // Empty state
            Box(
                modifier        = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment   = Alignment.CenterHorizontally,
                    verticalArrangement   = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier           = Modifier.size(64.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        text       = "No saved events yet",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text  = "Bookmark events to find them here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier        = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding  = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedEvents, key = { it.id }) { event ->
                    SavedEventCard(
                        event            = event,
                        onTap            = { navController.navigate(Screen.EventDetail.createRoute(event.id)) },
                        onRemoveBookmark = { eventViewModel.removeBookmark(userId, event.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedEventCard(
    event: EventItem,
    onTap: () -> Unit,
    onRemoveBookmark: () -> Unit
) {
    Card(
        onClick   = onTap,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Thumbnail
            AsyncImage(
                model              = event.imageUrl.ifBlank { null },
                contentDescription = event.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            // Event info
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Status label for cancelled/postponed
                val eventStatus = event.eventStatus()
                if (eventStatus != EventStatus.ACTIVE) {
                    val statusColor = when (eventStatus) {
                        EventStatus.CANCELLED -> MaterialTheme.colorScheme.error
                        EventStatus.POSTPONED -> MaterialTheme.colorScheme.tertiary
                        else                  -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val statusText = if (eventStatus == EventStatus.POSTPONED)
                        "POSTPONED → NEW DATE BELOW"
                    else
                        event.status.uppercase()
                    Text(
                        text          = statusText,
                        fontSize      = 10.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = statusColor,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text      = event.title,
                    style     = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines  = 2,
                    overflow  = TextOverflow.Ellipsis,
                    color     = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text  = event.formattedDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text     = "${event.neighborhood} · ${event.zone}",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Remove bookmark icon
            IconButton(onClick = onRemoveBookmark) {
                Icon(
                    imageVector        = Icons.Default.Bookmark,
                    contentDescription = "Remove bookmark",
                    tint               = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}