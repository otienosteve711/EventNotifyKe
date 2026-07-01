package com.sc.eventnotifyke.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sc.eventnotifyke.models.EventStatus
import com.sc.eventnotifyke.navigation.Screen
import com.sc.eventnotifyke.ui.components.AppBottomBar
import com.sc.eventnotifyke.viewmodel.AuthViewModel
import com.sc.eventnotifyke.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    navController: NavController,
    eventId: String,
    eventViewModel: EventViewModel,
    authViewModel: AuthViewModel
) {
    val profile       by authViewModel.currentProfile.collectAsState()
    val isAdmin       = profile?.role == "admin"
    val userId        = profile?.uid ?: ""

    val selectedEvent by eventViewModel.selectedEvent.collectAsState()
    val bookmarkedIds by eventViewModel.bookmarkedEventIds.collectAsState()
    val isBookmarked  = bookmarkedIds.contains(eventId)

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        eventViewModel.loadEventById(eventId)
        eventViewModel.loadBookmarks(userId)
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title   = { Text("Delete Event") },
            text    = { Text("Are you sure you want to delete this event? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    eventViewModel.deleteEvent(eventId)
                    showDeleteDialog = false
                    navController.popBackStack()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (selectedEvent == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val event = selectedEvent!!

    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentRoute    = null,
                onHomeClick     = { navController.navigate(Screen.Home.route) },
                onBrowseClick   = { navController.navigate(Screen.Browse.route) },
                onMyEventsClick = { navController.navigate(Screen.MyEvents.route) },
                onProfileClick  = { navController.navigate(Screen.Profile.route) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Hero Image ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                AsyncImage(
                    model              = event.imageUrl.ifBlank { null },
                    contentDescription = event.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )

                // Gradient at bottom of image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                            )
                        )
                )

                // Back arrow overlaid on image
                IconButton(
                    onClick  = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector        = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint               = Color.White
                    )
                }

                // Status banner at bottom of image
                val eventStatus = event.eventStatus()
                if (eventStatus != EventStatus.ACTIVE) {
                    val bannerColor = when (eventStatus) {
                        EventStatus.CANCELLED -> MaterialTheme.colorScheme.error
                        EventStatus.POSTPONED -> MaterialTheme.colorScheme.tertiary
                        else                  -> Color.Transparent
                    }
                    val statusLabel = when (eventStatus) {
                        EventStatus.CANCELLED -> "CANCELLED"
                        EventStatus.POSTPONED -> "POSTPONED"
                        else                  -> ""
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bannerColor.copy(alpha = 0.92f))
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Text(
                                text          = statusLabel,
                                color         = Color.White,
                                fontWeight    = FontWeight.Bold,
                                fontSize      = 12.sp,
                                letterSpacing = 1.5.sp
                            )
                            // Show the new date explicitly for postponed events,
                            // so users don't mistake it for the original date
                            if (eventStatus == EventStatus.POSTPONED) {
                                Text(
                                    text       = "Rescheduled to ${event.formattedDate()}",
                                    color      = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 13.sp
                                )
                            }
                            if (event.statusNote.isNotBlank()) {
                                Text(
                                    text     = event.statusNote,
                                    color    = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── Main Content ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // Title
                Text(
                    text       = event.title,
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )

                // Category chip
                AssistChip(
                    onClick = {},
                    label   = { Text(event.category, fontSize = 12.sp) },
                    colors  = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor     = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Date & Time
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📅", fontSize = 16.sp)
                    Column {
                        Text(
                            text       = event.formattedDate(),
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onBackground
                        )
                        if (event.time.isNotBlank()) {
                            Text(
                                text  = event.time,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Zone / Neighborhood
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📍", fontSize = 16.sp)
                    Column {
                        Text(
                            text       = event.neighborhood,
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text  = event.zone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Venue
                if (event.venue.isNotBlank()) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🏟️", fontSize = 16.sp)
                        Text(
                            text  = event.venue,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Ticket Price — uses formattedPrice() from EventItem
                if (!event.isFree()) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🎟️", fontSize = 16.sp)
                        Text(
                            text       = event.formattedPrice(),
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Description
                if (event.description.isNotBlank()) {
                    Text(
                        text       = "About this event",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text       = event.description,
                        style      = MaterialTheme.typography.bodyMedium,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }

                Spacer(Modifier.height(8.dp))

                // ── Bookmark Button ───────────────────────────────────────────
                Button(
                    onClick  = {
                        if (isBookmarked) eventViewModel.removeBookmark(userId, eventId)
                        else              eventViewModel.addBookmark(userId, eventId)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = if (isBookmarked)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector        = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier           = Modifier.size(18.dp),
                        tint               = if (isBookmarked)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text  = if (isBookmarked) "Bookmarked" else "Bookmark",
                        color = if (isBookmarked)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onPrimary
                    )
                }

                // ── Admin: Edit + Delete ──────────────────────────────────────
                if (isAdmin) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick  = { navController.navigate(Screen.EditEvent.createRoute(eventId)) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Edit")
                        }

                        Button(
                            onClick  = { showDeleteDialog = true },
                            modifier = Modifier.weight(1f),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Delete")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}