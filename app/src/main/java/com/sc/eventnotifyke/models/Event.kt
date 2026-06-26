package com.sc.eventnotifyke.models

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

enum class EventStatus { ACTIVE, CANCELLED, POSTPONED }

data class EventItem(
    val id           : String    = "",
    val title        : String    = "",
    val description  : String    = "",
    val date         : Timestamp = Timestamp.now(),   // Firestore Timestamp
    val time         : String    = "",
    val venue        : String    = "",
    val neighborhood : String    = "",
    val zone         : String    = "",
    val category     : String    = "",
    val imageUrl     : String    = "",
    val ticketPrice  : Int       = 0,
    val status       : String    = "active",
    val statusNote   : String    = "",                // reason for cancel/postpone
    val postedBy     : String    = "",
    val createdAt    : Timestamp = Timestamp.now()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id"           to id,
        "title"        to title,
        "description"  to description,
        "date"         to date,                       // saved as Firestore Timestamp
        "time"         to time,
        "venue"        to venue,
        "neighborhood" to neighborhood,
        "zone"         to zone,
        "category"     to category,
        "imageUrl"     to imageUrl,
        "ticketPrice"  to ticketPrice,
        "status"       to status,
        "statusNote"   to statusNote,
        "postedBy"     to postedBy,
        "createdAt"    to createdAt
    )

    // ── Status helpers ────────────────────────────────────────────────────────
    fun eventStatus(): EventStatus = when (status) {
        "cancelled" -> EventStatus.CANCELLED
        "postponed" -> EventStatus.POSTPONED
        else        -> EventStatus.ACTIVE
    }

    // ── Display helpers ───────────────────────────────────────────────────────
    fun formattedDate(): String {
        if (date == Timestamp(0, 0)) return "Date TBA"
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        return sdf.format(date.toDate())
    }

    fun isFree(): Boolean = ticketPrice == 0

    fun formattedPrice(): String =
        if (isFree()) "Free Entry" else "KES $ticketPrice"
}