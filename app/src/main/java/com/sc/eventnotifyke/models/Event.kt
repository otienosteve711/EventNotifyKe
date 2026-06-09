package com.sc.eventnotifyke.models

enum class EventStatus { ACTIVE, CANCELLED }

data class EventItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",          // e.g., "2026-06-18"
    val time: String = "",          // e.g., "09:00"
    val venue: String = "",         // e.g., "KICC"
    val neighborhood: String = "",  // e.g., "CBD", "Westlands"
    val category: String = "",      // e.g., "Tech", "Nightlife"
    val imageUrl: String = "",      // Flyer link
    val ticketPrice: Int = 0,       // 0 for Free Entry
    val status: String = "active"
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "title" to title,
        "description" to description,
        "date" to date,
        "time" to time,
        "venue" to venue,
        "neighborhood" to neighborhood,
        "category" to category,
        "imageUrl" to imageUrl,
        "ticketPrice" to ticketPrice,
        "status" to status
    )

    fun eventStatus(): EventStatus =
        if (status == "cancelled") EventStatus.CANCELLED else EventStatus.ACTIVE
}

