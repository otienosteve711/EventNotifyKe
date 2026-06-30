package com.sc.eventnotifyke.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sc.eventnotifyke.models.EventItem
import com.sc.eventnotifyke.utils.CloudinaryUploader
import com.sc.eventnotifyke.utils.zoneNeighborhoods
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ─── State ────────────────────────────────────────────────────────────────────

sealed class EventState {
    object Idle    : EventState()
    object Loading : EventState()
    object Success : EventState()
    data class Error(val message: String) : EventState()
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

class EventViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    // ── Read State ────────────────────────────────────────────────────────────

    private val _activeEvents  = MutableStateFlow<List<EventItem>>(emptyList())
    val activeEvents: StateFlow<List<EventItem>> = _activeEvents

    private val _allEvents     = MutableStateFlow<List<EventItem>>(emptyList())
    val allEvents: StateFlow<List<EventItem>> = _allEvents

    private val _selectedEvent = MutableStateFlow<EventItem?>(null)
    val selectedEvent: StateFlow<EventItem?> = _selectedEvent

    private val _postSuccess   = MutableStateFlow(false)
    val postSuccess: StateFlow<Boolean> = _postSuccess

    private val _userZone      = MutableStateFlow<String?>(null)

    // ── Post / Edit Form State ────────────────────────────────────────────────

    val title            = MutableStateFlow("")
    val description      = MutableStateFlow("")
    val date             = MutableStateFlow<Timestamp?>(null)
    val time             = MutableStateFlow("")
    val venue            = MutableStateFlow("")
    val neighborhood     = MutableStateFlow("")
    val zone             = MutableStateFlow("")
    val category         = MutableStateFlow("")
    val ticketPrice      = MutableStateFlow("0")
    val status           = MutableStateFlow("active")   // ← NEW
    val statusNote       = MutableStateFlow("")          // ← NEW
    val selectedImageUri = MutableStateFlow<Uri?>(null)
    val existingImageUrl = MutableStateFlow("")

    // ── Shared UI State ───────────────────────────────────────────────────────

    private val _eventState     = MutableStateFlow<EventState>(EventState.Idle)
    val eventState: StateFlow<EventState> = _eventState

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress

    // ── Filter State ──────────────────────────────────────────────────────────

    private val _selectedNeighborhood = MutableStateFlow("All")
    val selectedNeighborhood: StateFlow<String> = _selectedNeighborhood

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    // ── Bookmark State ────────────────────────────────────────────────────────

    private val _bookmarkedEventIds = MutableStateFlow<Set<String>>(emptySet())
    val bookmarkedEventIds: StateFlow<Set<String>> = _bookmarkedEventIds

    // ─── Load Functions ───────────────────────────────────────────────────────

    fun loadActiveEvents() {
        viewModelScope.launch {
            _eventState.value = EventState.Loading
            try {
                val now = Timestamp.now()

                val activeSnapshot = db.collection("events")
                    .whereEqualTo("status", "active")
                    .whereGreaterThanOrEqualTo("date", now)
                    .orderBy("date", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val activeEvents = activeSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(EventItem::class.java)?.copy(id = doc.id)
                }

                val otherSnapshot = db.collection("events")
                    .whereIn("status", listOf("cancelled", "postponed"))
                    .orderBy("date", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val otherEvents = otherSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(EventItem::class.java)?.copy(id = doc.id)
                }

                _activeEvents.value = (activeEvents + otherEvents)
                    .sortedBy { it.date }

                _eventState.value = EventState.Success
            } catch (e: Exception) {
                _eventState.value = EventState.Error(e.message ?: "Failed to load events")
            }
        }
    }

    fun loadAllEvents() {
        viewModelScope.launch {
            _eventState.value = EventState.Loading
            try {
                val snapshot = db.collection("events")
                    .orderBy("date", Query.Direction.ASCENDING)
                    .get()
                    .await()

                _allEvents.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(EventItem::class.java)?.copy(id = doc.id)
                }
                _eventState.value = EventState.Success
            } catch (e: Exception) {
                _eventState.value = EventState.Error(e.message ?: "Failed to load events")
            }
        }
    }

    fun loadEventById(eventId: String) {
        viewModelScope.launch {
            _eventState.value = EventState.Loading
            try {
                val doc = db.collection("events")
                    .document(eventId)
                    .get()
                    .await()

                _selectedEvent.value = doc.toObject(EventItem::class.java)?.copy(id = doc.id)
                _eventState.value = EventState.Success
            } catch (e: Exception) {
                _eventState.value = EventState.Error(e.message ?: "Failed to load event")
            }
        }
    }

    // ─── Post / Edit Functions ────────────────────────────────────────────────

    fun loadEventForEdit(event: EventItem) {
        title.value            = event.title
        description.value      = event.description
        date.value             = event.date
        time.value             = event.time
        venue.value            = event.venue
        neighborhood.value     = event.neighborhood
        zone.value             = event.zone
        category.value         = event.category
        ticketPrice.value      = event.ticketPrice.toString()
        status.value           = event.status        // ← NEW
        statusNote.value       = event.statusNote    // ← NEW
        existingImageUrl.value = event.imageUrl
    }

    fun postEvent(context: Context) {
        val uid = auth.currentUser?.uid ?: return
        if (!validateFields()) return

        viewModelScope.launch {
            _eventState.value = EventState.Loading
            try {
                val imageUrl = selectedImageUri.value?.let { uri ->
                    CloudinaryUploader.uploadImage(
                        context    = context,
                        imageUri   = uri,
                        preset     = "eventnotify_preset",
                        onProgress = { progress -> _uploadProgress.value = progress }
                    )
                } ?: ""

                val event = EventItem(
                    title        = title.value.trim(),
                    description  = description.value.trim(),
                    date         = date.value!!,
                    time         = time.value,
                    venue        = venue.value.trim(),
                    neighborhood = neighborhood.value,
                    zone         = zone.value,
                    category     = category.value,
                    imageUrl     = imageUrl,
                    ticketPrice  = ticketPrice.value.toIntOrNull() ?: 0,
                    status       = "active",
                    postedBy     = uid
                )

                val docRef = db.collection("events").add(event.toMap()).await()
                db.collection("events").document(docRef.id).update("id", docRef.id).await()

                _postSuccess.value = true
                _eventState.value  = EventState.Success

            } catch (e: Exception) {
                _eventState.value = EventState.Error(e.message ?: "Failed to post event")
            } finally {
                _uploadProgress.value = 0f
            }
        }
    }

    fun updateEvent(context: Context, eventId: String) {
        if (!validateFields()) return

        viewModelScope.launch {
            _eventState.value = EventState.Loading
            try {
                val imageUrl = selectedImageUri.value?.let { uri ->
                    CloudinaryUploader.uploadImage(
                        context    = context,
                        imageUri   = uri,
                        preset     = "eventnotify_preset",
                        onProgress = { progress -> _uploadProgress.value = progress }
                    )
                } ?: existingImageUrl.value

                val updatedFields = mapOf(
                    "title"        to title.value.trim(),
                    "description"  to description.value.trim(),
                    "date"         to date.value!!,
                    "time"         to time.value,
                    "venue"        to venue.value.trim(),
                    "neighborhood" to neighborhood.value,
                    "zone"         to zone.value,
                    "category"     to category.value,
                    "imageUrl"     to imageUrl,
                    "ticketPrice"  to (ticketPrice.value.toIntOrNull() ?: 0),
                    "status"       to status.value,       // ← NEW
                    "statusNote"   to statusNote.value    // ← NEW
                )

                db.collection("events").document(eventId).update(updatedFields).await()

                _postSuccess.value = true
                _eventState.value  = EventState.Success

            } catch (e: Exception) {
                _eventState.value = EventState.Error(e.message ?: "Failed to update event")
            } finally {
                _uploadProgress.value = 0f
            }
        }
    }

    // ─── Cancel / Postpone Functions ──────────────────────────────────────────

    fun cancelEvent(eventId: String, reason: String = "") {
        viewModelScope.launch {
            try {
                db.collection("events").document(eventId).update(
                    mapOf("status" to "cancelled", "statusNote" to reason)
                ).await()
                loadActiveEvents()
            } catch (e: Exception) {
                _eventState.value = EventState.Error(e.message ?: "Failed to cancel event")
            }
        }
    }

    fun postponeEvent(eventId: String, newDate: Timestamp, reason: String = "") {
        viewModelScope.launch {
            try {
                db.collection("events").document(eventId).update(
                    mapOf(
                        "status"     to "postponed",
                        "date"       to newDate,
                        "statusNote" to reason
                    )
                ).await()
                loadActiveEvents()
            } catch (e: Exception) {
                _eventState.value = EventState.Error(e.message ?: "Failed to postpone event")
            }
        }
    }

    // ─── Delete Function ──────────────────────────────────────────────────────

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _eventState.value = EventState.Loading
            try {
                db.collection("events").document(eventId).delete().await()

                _activeEvents.value = _activeEvents.value.filter { it.id != eventId }
                _allEvents.value    = _allEvents.value.filter { it.id != eventId }

                _eventState.value = EventState.Success
            } catch (e: Exception) {
                _eventState.value = EventState.Error(e.message ?: "Failed to delete event")
            }
        }
    }

    // ─── Bookmark Functions ───────────────────────────────────────────────────

    fun loadBookmarks(userId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            db.collection("users")
                .document(userId)
                .collection("bookmarks")
                .addSnapshotListener { snapshot, _ ->
                    val ids = snapshot?.documents?.map { it.id }?.toSet() ?: emptySet()
                    _bookmarkedEventIds.value = ids
                }
        }
    }

    fun addBookmark(userId: String, eventId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            db.collection("users")
                .document(userId)
                .collection("bookmarks")
                .document(eventId)
                .set(mapOf("bookmarkedAt" to Timestamp.now()))
        }
    }

    fun removeBookmark(userId: String, eventId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            db.collection("users")
                .document(userId)
                .collection("bookmarks")
                .document(eventId)
                .delete()
        }
    }

    // ─── Filter Functions ─────────────────────────────────────────────────────

    fun setNeighborhoodFilter(neighborhood: String) {
        _selectedNeighborhood.value = neighborhood
    }

    fun setUserZone(zone: String) {
        _userZone.value = zone
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    val filteredEvents: StateFlow<List<EventItem>> = combine(
        _activeEvents,
        _selectedNeighborhood,
        _selectedCategory,
        _userZone
    ) { events, neighborhood, category, userZone ->

        val allowedNeighborhoods = zoneNeighborhoods[userZone] ?: emptyList()

        events.filter { event ->
            val neighborhoodMatch = when {
                userZone.isNullOrEmpty() -> true
                neighborhood == "All"    -> event.neighborhood in allowedNeighborhoods
                else                     -> event.neighborhood == neighborhood
            }
            val categoryMatch = category == "All" || event.category == category
            neighborhoodMatch && categoryMatch
        }
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ─── Utility Functions ────────────────────────────────────────────────────

    fun clearSelectedEvent() {
        _selectedEvent.value = null
    }

    fun resetFormFields() {
        title.value            = ""
        description.value      = ""
        date.value             = null
        time.value             = ""
        venue.value            = ""
        neighborhood.value     = ""
        zone.value             = ""
        category.value         = ""
        ticketPrice.value      = "0"
        status.value           = "active"   // ← NEW
        statusNote.value       = ""         // ← NEW
        selectedImageUri.value = null
        existingImageUrl.value = ""
    }

    fun clearState() {
        _eventState.value = EventState.Idle
    }

    fun resetPostSuccess() {
        _postSuccess.value = false
    }

    // ─── Validation ───────────────────────────────────────────────────────────

    private fun validateFields(): Boolean {
        return when {
            title.value.isBlank() -> {
                _eventState.value = EventState.Error("Title is required")
                false
            }
            date.value == null -> {
                _eventState.value = EventState.Error("Date is required")
                false
            }
            time.value.isBlank() -> {
                _eventState.value = EventState.Error("Time is required")
                false
            }
            venue.value.isBlank() -> {
                _eventState.value = EventState.Error("Venue is required")
                false
            }
            neighborhood.value.isBlank() -> {
                _eventState.value = EventState.Error("Neighborhood is required")
                false
            }
            category.value.isBlank() -> {
                _eventState.value = EventState.Error("Category is required")
                false
            }
            else -> true
        }
    }
}