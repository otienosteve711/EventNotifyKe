package com.sc.eventnotifyke.models

enum class EventStatus { ACTIVE, CANCELLED }

data class EventItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val venue: String = "",
    val neighborhood: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val ticketPrice: Int = 0,
    val status: String = "active",
    val postedBy: String = "",       // UID of admin who posted
    val createdAt: Long = 0L         // System.currentTimeMillis() — for sorting
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
        "status" to status,
        "postedBy" to postedBy,
        "createdAt" to createdAt
    )

    // Derived helpers
    fun eventStatus(): EventStatus =
        if (status == "cancelled") EventStatus.CANCELLED else EventStatus.ACTIVE

    fun isFree(): Boolean = ticketPrice == 0

    fun formattedPrice(): String =
        if (isFree()) "Free Entry" else "KES $ticketPrice"
}

