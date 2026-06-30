package com.sc.eventnotifyke.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sc.eventnotifyke.models.EventItem
import com.sc.eventnotifyke.models.EventStatus
import com.sc.eventnotifyke.navigation.Screen
import com.sc.eventnotifyke.ui.components.AppBottomBar
import com.sc.eventnotifyke.utils.zoneNeighborhoods
import com.sc.eventnotifyke.viewmodel.AuthViewModel
import com.sc.eventnotifyke.viewmodel.EventState
import com.sc.eventnotifyke.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    eventViewModel: EventViewModel = viewModel()
) {
    // ── Auth state ────────────────────────────────────────────────────────────
    val profile by authViewModel.currentProfile.collectAsState()
    val isAdmin = profile?.role == "admin"

    // ── User's zone from profile ──────────────────────────────────────────────
    val userZone = profile?.estate ?: ""

    // ── Neighborhoods for user's zone — "All" always first ───────────────────
    val neighborhoods = listOf("All") + (zoneNeighborhoods[userZone] ?: emptyList())

    // ── Event state ───────────────────────────────────────────────────────────
    val eventState by eventViewModel.eventState.collectAsState()
    val isLoading  = eventState is EventState.Loading
    val displayedEvents by eventViewModel.filteredEvents.collectAsState()

    // ── Local UI state ────────────────────────────────────────────────────────
    var selectedNeighborhood by remember { mutableStateOf("All") }
    var selectedCategory     by remember { mutableStateOf("All") }
    var searchQuery          by remember { mutableStateOf("") }
    var showSearch           by remember { mutableStateOf(false) }

    val categories = listOf(
        "All", "Music", "Sports", "Food", "Church",
        "Community", "Arts", "Business", "Education"
    )

    // ── Set zone filter + load on first launch ────────────────────────────────
    LaunchedEffect(userZone) {
        if (userZone.isNotEmpty()) {
            eventViewModel.setUserZone(userZone)
        }
    }

    // ── Re-fetch every time HomeScreen resumes ────────────────────────────────
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            eventViewModel.loadActiveEvents()
        }
    }

    // ── Update neighborhood filter when chip changes ──────────────────────────
    LaunchedEffect(selectedNeighborhood) {
        eventViewModel.setNeighborhoodFilter(selectedNeighborhood)
    }

    // ── Update category filter when chip changes ──────────────────────────────
    LaunchedEffect(selectedCategory) {
        eventViewModel.setCategoryFilter(selectedCategory)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        TextField(
                            value         = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder   = {
                                Text(
                                    "Search events...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            singleLine = true,
                            colors     = TextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedTextColor        = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor      = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                focusedIndicatorColor   = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                cursorColor             = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text       = "EventNotify KE",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showSearch = !showSearch
                        if (!showSearch) searchQuery = ""
                    }) {
                        Icon(
                            imageVector        = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint               = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            AppBottomBar(
                currentRoute    = "home",
                onHomeClick     = {},
                onBrowseClick   = { navController.navigate(Screen.Browse.route) },
                onMyEventsClick = { navController.navigate(Screen.MyEvents.route) },
                onProfileClick  = { navController.navigate(Screen.Profile.route) }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                ExtendedFloatingActionButton(
                    onClick        = { navController.navigate(Screen.PostEvent.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary,
                    icon           = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text           = { Text("Post Event", fontWeight = FontWeight.SemiBold) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // ── Greeting ──────────────────────────────────────────────────────
            profile?.let {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text       = "Hey ${it.fullname.split(" ").first()} 👋",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text  = if (selectedNeighborhood == "All")
                            "Explore events in $userZone"
                        else
                            "Explore events in $selectedNeighborhood",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Neighborhood filter chips ─────────────────────────────────────
            Text(
                text     = "Neighborhood",
                style    = MaterialTheme.typography.labelMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                items(neighborhoods) { hood ->
                    FilterChip(
                        selected = selectedNeighborhood == hood,
                        onClick  = { selectedNeighborhood = hood },
                        label    = { Text(hood, style = MaterialTheme.typography.labelSmall) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor     = MaterialTheme.colorScheme.onPrimary,
                            containerColor         = MaterialTheme.colorScheme.surface,
                            labelColor             = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Category filter chips ─────────────────────────────────────────
            Text(
                text     = "Category",
                style    = MaterialTheme.typography.labelMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick  = { selectedCategory = cat },
                        label    = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                            selectedLabelColor     = MaterialTheme.colorScheme.onTertiary,
                            containerColor         = MaterialTheme.colorScheme.surface,
                            labelColor             = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Events count header ───────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Upcoming Events",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text  = "${displayedEvents.size} found",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Events feed ───────────────────────────────────────────────────
            when {
                isLoading -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                displayedEvents.isEmpty() -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🎉", style = MaterialTheme.typography.displaySmall)
                            Text(
                                text       = "No events in this area yet.",
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text  = "Check back soon or explore other zones.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    // Apply search filter locally
                    val filteredBySearch = if (searchQuery.isBlank()) displayedEvents
                    else displayedEvents.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                                it.neighborhood.contains(searchQuery, ignoreCase = true) ||
                                it.category.contains(searchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier            = Modifier.fillMaxSize()
                    ) {
                        items(filteredBySearch) { event ->
                            EventCard(
                                event   = event,
                                onClick = {
                                    navController.navigate(
                                        Screen.EventDetail.createRoute(event.id)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Event Card ───────────────────────────────────────────────────────────────

@Composable
fun EventCard(
    event: EventItem,
    onClick: () -> Unit
) {
    val eventStatus = event.eventStatus()

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {

            // ── Event image ───────────────────────────────────────────────────
            AsyncImage(
                model              = event.imageUrl,
                contentDescription = event.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )

            // ── Status banner (only for cancelled / postponed) ────────────────
            if (eventStatus != EventStatus.ACTIVE) {
                val bannerColor = when (eventStatus) {
                    EventStatus.CANCELLED -> MaterialTheme.colorScheme.error
                    EventStatus.POSTPONED -> MaterialTheme.colorScheme.tertiary
                    else                  -> Color.Transparent
                }
                val bannerLabel = when (eventStatus) {
                    EventStatus.CANCELLED -> "⚠ CANCELLED"
                    EventStatus.POSTPONED -> "🕐 POSTPONED"
                    else                  -> ""
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bannerColor)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text          = bannerLabel,
                        color         = Color.White,
                        fontWeight    = FontWeight.Bold,
                        fontSize      = 11.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {

                // ── Category badge ────────────────────────────────────────────
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text     = event.category,
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // ── Title ─────────────────────────────────────────────────────
                Text(
                    text       = event.title,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // ── Date & time ───────────────────────────────────────────────
                Text(
                    text  = "${event.formattedDate()} • ${event.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                // ── Venue & neighborhood ──────────────────────────────────────
                Text(
                    text     = "${event.venue}, ${event.neighborhood}",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ── Price badge ───────────────────────────────────────────────
                Surface(
                    color = if (event.isFree())
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text       = event.formattedPrice(),
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (event.isFree())
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}