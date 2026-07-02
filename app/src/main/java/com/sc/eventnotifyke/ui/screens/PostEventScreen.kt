package com.sc.eventnotifyke.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.sc.eventnotifyke.ui.theme.appTextFieldColors
import com.sc.eventnotifyke.utils.eventCategories
import com.sc.eventnotifyke.utils.zoneNeighborhoods
import com.sc.eventnotifyke.viewmodel.AuthViewModel
import com.sc.eventnotifyke.viewmodel.EventState
import com.sc.eventnotifyke.viewmodel.EventViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostEventScreen(
    navController: NavController,
    eventId: String? = null,
    authViewModel: AuthViewModel = viewModel(),
    eventViewModel: EventViewModel = viewModel()
) {
    val context    = LocalContext.current
    val isEditMode = eventId != null

    // ── Collect ViewModel flows ───────────────────────────────────────────────
    val title        by eventViewModel.title.collectAsState()
    val description  by eventViewModel.description.collectAsState()
    val date         by eventViewModel.date.collectAsState()
    val time         by eventViewModel.time.collectAsState()
    val venue        by eventViewModel.venue.collectAsState()
    val neighborhood by eventViewModel.neighborhood.collectAsState()
    val vmZone       by eventViewModel.zone.collectAsState()
    val category     by eventViewModel.category.collectAsState()
    val ticketPrice  by eventViewModel.ticketPrice.collectAsState()
    val status       by eventViewModel.status.collectAsState()
    val statusNote   by eventViewModel.statusNote.collectAsState()
    val imageUri     by eventViewModel.selectedImageUri.collectAsState()

    val eventState     by eventViewModel.eventState.collectAsState()
    val uploadProgress by eventViewModel.uploadProgress.collectAsState()

    val isLoading    = eventState is EventState.Loading
    val errorMessage = (eventState as? EventState.Error)?.message

    // categories now sourced from utils/EventCategories.kt — single source of truth

    // ── Date picker state ─────────────────────────────────────────────────────
    var showDatePicker  by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = date?.toDate()?.time
    )

    // ── Time picker state ─────────────────────────────────────────────────────
    var showTimePicker  by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour   = 18,   // default 6 PM — sensible default for evening events
        initialMinute = 0,
        is24Hour      = false
    )

    // ── Zone picker ───────────────────────────────────────────────────────────
    var selectedZone by remember(vmZone) { mutableStateOf(vmZone) }

    // ── Pre-fill fields when editing ──────────────────────────────────────────
    val selectedEvent by eventViewModel.selectedEvent.collectAsState()
    LaunchedEffect(eventId) {
        if (isEditMode) {
            selectedEvent?.let { eventViewModel.loadEventForEdit(it) }
                ?: eventViewModel.loadEventById(eventId!!)
        } else {
            eventViewModel.resetFormFields()
        }
    }

    LaunchedEffect(selectedEvent) {
        if (isEditMode && selectedEvent != null) {
            eventViewModel.loadEventForEdit(selectedEvent!!)
            selectedZone = selectedEvent!!.zone.ifBlank {
                zoneNeighborhoods.entries
                    .firstOrNull { selectedEvent!!.neighborhood in it.value }
                    ?.key ?: ""
            }
        }
    }

    // ── Navigate back on success ──────────────────────────────────────────────
    val postSuccess by eventViewModel.postSuccess.collectAsState()
    LaunchedEffect(postSuccess) {
        if (postSuccess) {
            eventViewModel.resetPostSuccess()
            eventViewModel.clearState()
            eventViewModel.resetFormFields()
            navController.popBackStack()
        }
    }

    // ── Image picker ──────────────────────────────────────────────────────────
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> eventViewModel.selectedImageUri.value = uri }

    // ── Date display label ────────────────────────────────────────────────────
    val dateLabel = date?.let { ts ->
        java.text.SimpleDateFormat("EEE, dd MMM yyyy", java.util.Locale.getDefault())
            .format(ts.toDate())
    } ?: "Select event date"

    // ── DatePickerDialog ──────────────────────────────────────────────────────
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        eventViewModel.date.value = Timestamp(Date(millis))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ── TimePickerDialog ──────────────────────────────────────────────────────
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val hour24   = timePickerState.hour
                    val minute   = timePickerState.minute
                    val amPm     = if (hour24 < 12) "AM" else "PM"
                    val hour12   = when {
                        hour24 == 0  -> 12
                        hour24 > 12  -> hour24 - 12
                        else         -> hour24
                    }
                    val formatted = String.format("%d:%02d %s", hour12, minute, amPm)
                    eventViewModel.time.value = formatted
                    showTimePicker = false
                }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Event" else "Post Event",
                        color      = MaterialTheme.colorScheme.onSecondary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Error card ────────────────────────────────────────────────────
            errorMessage?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }

            // ── Image picker ──────────────────────────────────────────────────
            SectionLabel("Event Banner")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        2.dp,
                        if (imageUri != null) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { imageLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle, null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            imageUri.toString(),
                            style     = MaterialTheme.typography.labelSmall,
                            color     = MaterialTheme.colorScheme.onSurface,
                            maxLines  = 2,
                            overflow  = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.Image, null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (isEditMode) "Tap to replace banner image"
                            else "Tap to select banner image",
                            style     = MaterialTheme.typography.labelSmall,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // ── Event Details ─────────────────────────────────────────────────
            SectionLabel("Event Details")

            OutlinedTextField(
                value         = title,
                onValueChange = { eventViewModel.title.value = it },
                label         = { Text("Event Title") },
                leadingIcon   = { Icon(Icons.Default.Title, null) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                colors        = appTextFieldColors()
            )

            OutlinedTextField(
                value         = description,
                onValueChange = { eventViewModel.description.value = it },
                label         = { Text("Description") },
                leadingIcon   = { Icon(Icons.Default.Description, null) },
                minLines      = 3,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                colors        = appTextFieldColors()
            )

            // ── Date & Time ───────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value         = dateLabel,
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Date") },
                    leadingIcon   = { Icon(Icons.Default.CalendarToday, null) },
                    modifier      = Modifier
                        .weight(1f)
                        .clickable { showDatePicker = true },
                    shape         = RoundedCornerShape(12.dp),
                    colors        = appTextFieldColors(),
                    enabled       = false
                )

                OutlinedTextField(
                    value         = time.ifBlank { "Select time" },
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Time") },
                    leadingIcon   = { Icon(Icons.Default.Schedule, null) },
                    modifier      = Modifier
                        .weight(1f)
                        .clickable { showTimePicker = true },
                    shape         = RoundedCornerShape(12.dp),
                    colors        = appTextFieldColors(),
                    enabled       = false
                )
            }

            // ── Venue ─────────────────────────────────────────────────────────
            OutlinedTextField(
                value         = venue,
                onValueChange = { eventViewModel.venue.value = it },
                label         = { Text("Venue") },
                leadingIcon   = { Icon(Icons.Default.LocationOn, null) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                colors        = appTextFieldColors()
            )

            // ── Ticket Price ──────────────────────────────────────────────────
            OutlinedTextField(
                value           = ticketPrice,
                onValueChange   = { eventViewModel.ticketPrice.value = it },
                label           = { Text("Ticket Price (KES) — leave blank for Free") },
                leadingIcon     = { Icon(Icons.Default.AttachMoney, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(12.dp),
                colors          = appTextFieldColors()
            )

            // ── Category chips ────────────────────────────────────────────────
            SectionLabel("Category")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(4.dp)
            ) {
                eventCategories.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick  = { eventViewModel.category.value = cat },
                        label    = { Text(cat) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // ── Zone chips ────────────────────────────────────────────────────
            SectionLabel("Zone")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(4.dp)
            ) {
                zoneNeighborhoods.keys.forEach { z ->
                    FilterChip(
                        selected = selectedZone == z,
                        onClick  = {
                            selectedZone = z
                            eventViewModel.zone.value = z
                            eventViewModel.neighborhood.value = ""
                        },
                        label  = { Text(z) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor     = MaterialTheme.colorScheme.onSecondary
                        )
                    )
                }
            }

            // ── Neighborhood chips ────────────────────────────────────────────
            if (selectedZone.isNotEmpty()) {
                SectionLabel("Neighborhood")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(4.dp)
                ) {
                    zoneNeighborhoods[selectedZone]?.forEach { nb ->
                        FilterChip(
                            selected = neighborhood == nb,
                            onClick  = { eventViewModel.neighborhood.value = nb },
                            label    = { Text(nb) },
                            leadingIcon = if (neighborhood == nb) {
                                { Icon(Icons.Default.Map, null, Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor   = MaterialTheme.colorScheme.primary,
                                selectedLabelColor       = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.tertiary
                            )
                        )
                    }
                }
            }

            // ── Status section (edit mode only) ───────────────────────────────
            if (isEditMode) {
                SectionLabel("Event Status")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("active", "postponed", "cancelled").forEach { s ->
                        val chipColor = when (s) {
                            "cancelled" -> MaterialTheme.colorScheme.error
                            "postponed" -> MaterialTheme.colorScheme.tertiary
                            else        -> MaterialTheme.colorScheme.primary
                        }
                        FilterChip(
                            selected = status == s,
                            onClick  = {
                                eventViewModel.status.value = s
                                // Clear statusNote when switching back to active
                                if (s == "active") eventViewModel.statusNote.value = ""
                            },
                            label  = { Text(s.replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColor,
                                selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                // Reason field — only shown when cancelled or postponed
                if (status == "cancelled" || status == "postponed") {
                    OutlinedTextField(
                        value         = statusNote,
                        onValueChange = { eventViewModel.statusNote.value = it },
                        label         = {
                            Text(
                                if (status == "cancelled") "Cancellation Reason"
                                else "Postponement Reason"
                            )
                        },
                        placeholder   = {
                            Text(
                                if (status == "cancelled") "e.g. Venue unavailable"
                                else "e.g. Rescheduled to next week"
                            )
                        },
                        minLines      = 2,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(12.dp),
                        colors        = appTextFieldColors()
                    )
                }
            }

            // ── Upload progress ───────────────────────────────────────────────
            if (isLoading && uploadProgress > 0f) {
                Column {
                    LinearProgressIndicator(
                        progress   = { uploadProgress },
                        modifier   = Modifier.fillMaxWidth(),
                        color      = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Uploading image… ${(uploadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // ── Submit button ─────────────────────────────────────────────────
            val formValid = title.isNotBlank()       &&
                    description.isNotBlank()         &&
                    date != null                     &&
                    time.isNotBlank()                &&
                    venue.isNotBlank()               &&
                    neighborhood.isNotBlank()        &&
                    category.isNotBlank()            &&
                    (imageUri != null || isEditMode) &&
                    !isLoading

            Button(
                onClick = {
                    if (isEditMode) eventViewModel.updateEvent(context, eventId!!)
                    else            eventViewModel.postEvent(context)
                },
                enabled  = formValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isEditMode) "Save Changes" else "Post Event",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Reusable section label ────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style      = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color      = MaterialTheme.colorScheme.onBackground
    )
}