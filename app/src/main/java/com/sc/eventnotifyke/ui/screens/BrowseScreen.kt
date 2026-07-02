package com.sc.eventnotifyke.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.sc.eventnotifyke.navigation.Screen
import com.sc.eventnotifyke.ui.components.AppBottomBar
import com.sc.eventnotifyke.utils.eventCategoriesWithAll
import com.sc.eventnotifyke.utils.zoneNeighborhoods
import com.sc.eventnotifyke.viewmodel.EventState
import com.sc.eventnotifyke.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    navController: NavController,
    eventViewModel: EventViewModel
) {
    // ── All zones from ZoneData ───────────────────────────────────────────────
    val allZones = listOf("All") + zoneNeighborhoods.keys.toList()

    val categories = eventCategoriesWithAll   // sourced from utils/EventCategories.kt

    // ── Local UI state ────────────────────────────────────────────────────────
    var selectedZone     by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery      by remember { mutableStateOf("") }
    var showSearch        by remember { mutableStateOf(false) }   // ← NEW: collapsed by default

    // ── Event state ───────────────────────────────────────────────────────────
    val eventState     by eventViewModel.eventState.collectAsState()
    val isLoading       = eventState is EventState.Loading
    val allEvents      by eventViewModel.filteredEvents.collectAsState()

    // ── Filter events for BrowseScreen locally ────────────────────────────────
    val displayedEvents = remember(allEvents, selectedZone, selectedCategory, searchQuery) {
        allEvents.filter { event ->
            val zoneMatch = selectedZone == "All" || event.zone == selectedZone
            val categoryMatch = selectedCategory == "All" || event.category == selectedCategory
            val searchMatch   = searchQuery.isBlank() ||
                    event.title.contains(searchQuery, ignoreCase = true) ||
                    event.venue.contains(searchQuery, ignoreCase = true) ||
                    event.neighborhood.contains(searchQuery, ignoreCase = true)
            zoneMatch && categoryMatch && searchMatch
        }
    }

    // ── Load ALL events (no zone pre-filter) on resume ────────────────────────
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            eventViewModel.setUserZone("")
            eventViewModel.setNeighborhoodFilter("All")
            eventViewModel.setCategoryFilter("All")
            eventViewModel.loadActiveEvents()
        }
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
                                    "Search events, venues, areas…",
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
                            text       = "Browse Events",
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
                currentRoute    = "browse",
                onHomeClick     = { navController.navigate(Screen.Home.route) },
                onBrowseClick   = {},
                onMyEventsClick = { navController.navigate(Screen.MyEvents.route) },
                onProfileClick  = { navController.navigate(Screen.Profile.route) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // ── Zone filter chips ─────────────────────────────────────────────
            Text(
                text     = "Zone",
                style    = MaterialTheme.typography.labelMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                items(allZones) { zone ->
                    FilterChip(
                        selected = selectedZone == zone,
                        onClick  = { selectedZone = zone },
                        label    = { Text(zone, style = MaterialTheme.typography.labelSmall) },
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

            // ── Results count ─────────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "All Nairobi Events",
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

            // ── Events list ───────────────────────────────────────────────────
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
                            Text(
                                text  = "🔍",
                                style = MaterialTheme.typography.displaySmall
                            )
                            Text(
                                text       = "No events match your search.",
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text  = "Try a different zone, category, or keyword.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier            = Modifier.fillMaxSize()
                    ) {
                        items(displayedEvents) { event ->
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