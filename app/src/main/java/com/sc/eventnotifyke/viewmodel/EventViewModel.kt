package com.sc.eventnotifyke.viewmodel


import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sc.eventnotifyke.models.EventItem
import com.sc.eventnotifyke.utils.CloudinaryUploader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import com.sc.eventnotifyke.utils.zoneNeighborhoods

// ─── State ───────────────────────────────────────────────────────────────────

sealed class EventState {
    object Idle : EventState()
    object Loading : EventState()
    object Success : EventState()
    data class Error(val message: String) : EventState()
}

// ─── ViewModel ───────────────────────────────────────────────────────────────

class EventViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // ── Read State ────────────────────────────────────────────────────────────

    private val _activeEvents = MutableStateFlow<List<EventItem>>(emptyList())
    val activeEvents: StateFlow<List<EventItem>> = _activeEvents

    private val _allEvents = MutableStateFlow<List<EventItem>>(emptyList())
    val allEvents: StateFlow<List<EventItem>> = _allEvents

    private val _selectedEvent = MutableStateFlow<EventItem?>(null)
    val selectedEvent: StateFlow<EventItem?> = _selectedEvent
    private val _postSuccess = MutableStateFlow(false)
    private val _userZone = MutableStateFlow<String?>(null)

    val postSuccess: StateFlow<Boolean> = _postSuccess

    // ── Post/Edit Form State ──────────────────────────────────────────────────

    val title = MutableStateFlow("")
    val description = MutableStateFlow("")
    val date = MutableStateFlow("")
    val time = MutableStateFlow("")
    val venue = MutableStateFlow("")
    val neighborhood = MutableStateFlow("")
    val category = MutableStateFlow("")
    val ticketPrice = MutableStateFlow("0")
    val selectedImageUri = MutableStateFlow<Uri?>(null)
    val existingImageUrl = MutableStateFlow("")

    // ── Shared UI State ───────────────────────────────────────────────────────

    private val _eventState = MutableStateFlow<EventState>(EventState.Idle)
    val eventState: StateFlow<EventState> = _eventState

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress

    // ── Filter State ──────────────────────────────────────────────────────────

    private val _selectedNeighborhood = MutableStateFlow("All")
    val selectedNeighborhood: StateFlow<String> = _selectedNeighborhood

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    // ─── Load Functions ───────────────────────────────────────────────────────

    fun loadActiveEvents() {
        viewModelScope.launch {
            _eventState.value = EventState.Loading
            try {
                val snapshot = db.collection("events")
                    .whereEqualTo("status", "active")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                _activeEvents.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(EventItem::class.java)?.copy(id = doc.id)
                }
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
                    .orderBy("createdAt", Query.Direction.DESCENDING)
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
        title.value = event.title
        description.value = event.description
        date.value = event.date
        time.value = event.time
        venue.value = event.venue
        neighborhood.value = event.neighborhood
        category.value = event.category
        ticketPrice.value = event.ticketPrice.toString()
        existingImageUrl.value = event.imageUrl
    }

    fun postEvent(context: Context) {
        val uid = auth.currentUser?.uid ?: return
        if (!validateFields()) return

        viewModelScope.launch {
            _eventState.value = EventState.Loading
            try {
                // 1. Upload image to Cloudinary if selected
                val imageUrl = selectedImageUri.value?.let { uri ->
                    CloudinaryUploader.uploadImage(
                        context = context,
                        imageUri = uri,
                        preset = "eventnotify_preset",
                        onProgress = { progress -> _uploadProgress.value = progress }
                    )
                } ?: ""

                // 2. Build EventItem
                val event = EventItem(
                    title = title.value.trim(),
                    description = description.value.trim(),
                    date = date.value,
                    time = time.value,
                    venue = venue.value.trim(),
                    neighborhood = neighborhood.value,
                    category = category.value,
                    imageUrl = imageUrl,
                    ticketPrice = ticketPrice.value.toIntOrNull() ?: 0,
                    status = "active",
                    postedBy = uid,
                    createdAt = System.currentTimeMillis()
                )

                // 3. Save to Firestore
                val docRef = db.collection("events").add(event.toMap()).await()

                // 4. Write generated ID back into the document
                db.collection("events").document(docRef.id).update("id", docRef.id).await()

                _eventState.value = EventState.Success
                _postSuccess.value=true

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
                // 1. Upload new image if selected, else keep existing
                val imageUrl = selectedImageUri.value?.let { uri ->
                    CloudinaryUploader.uploadImage(
                        context = context,
                        imageUri = uri,
                        preset = "eventnotify_preset",
                        onProgress = { progress -> _uploadProgress.value = progress }
                    )
                } ?: existingImageUrl.value

                // 2. Build update map
                val updatedFields = mapOf(
                    "title" to title.value.trim(),
                    "description" to description.value.trim(),
                    "date" to date.value,
                    "time" to time.value,
                    "venue" to venue.value.trim(),
                    "neighborhood" to neighborhood.value,
                    "category" to category.value,
                    "imageUrl" to imageUrl,
                    "ticketPrice" to (ticketPrice.value.toIntOrNull() ?: 0)
                )

                // 3. Update in Firestore
                db.collection("events").document(eventId).update(updatedFields).await()

                _eventState.value = EventState.Success
                _postSuccess.value = true

            } catch (e: Exception) {
                _eventState.value = EventState.Error(e.message ?: "Failed to update event")
            } finally {
                _uploadProgress.value = 0f
            }
        }
    }

    // ─── Delete Function ──────────────────────────────────────────────────────

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _eventState.value = EventState.Loading
            try {
                db.collection("events").document(eventId).delete().await()

                // Optimistic update — remove from local lists immediately
                _activeEvents.value = _activeEvents.value.filter { it.id != eventId }
                _allEvents.value = _allEvents.value.filter { it.id != eventId }

                _eventState.value = EventState.Success
            } catch (e: Exception) {
                _eventState.value = EventState.Error(e.message ?: "Failed to delete event")
            }
        }
    }

    // ─── Filter Functions ─────────────────────────────────────────────────────

    fun setNeighborhoodFilter(neighborhood: String) {
        _selectedNeighborhood.value = neighborhood
    }
    fun setUserZone(zone: String) {
        _userZone.value = zone }



    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    // 3. Now the combine block can see everything correctly
    val filteredEvents: StateFlow<List<EventItem>> = combine(
        _activeEvents,
        _selectedNeighborhood,
        _selectedCategory,
        _userZone // This should be set when the user logs in
    ) { events, neighborhood, category, userZone ->

        // Determine the list of neighborhoods allowed in the current zone
        val allowedNeighborhoods = zoneNeighborhoods[userZone] ?: emptyList()

        events.filter { event ->
            // Neighborhood logic
            val neighborhoodMatch = when (neighborhood) {
                "All" -> event.neighborhood in allowedNeighborhoods
                else -> event.neighborhood == neighborhood
            }

            // Category logic
            val categoryMatch = category == "All" || event.category == category

            neighborhoodMatch && categoryMatch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ─── Utility Functions ────────────────────────────────────────────────────

    fun clearSelectedEvent() {
        _selectedEvent.value = null
    }


    fun resetFormFields() {
        title.value = ""
        description.value = ""
        date.value = ""
        time.value = ""
        venue.value = ""
        neighborhood.value = ""
        category.value = ""
        ticketPrice.value = "0"
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
            date.value.isBlank() -> {
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