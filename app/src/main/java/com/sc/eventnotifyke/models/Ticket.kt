package com.sc.eventnotifyke.models

data class TicketItem(
    val ticketId: String = "",
    val eventId: String = "",
    val buyerName: String = "",
    val buyerPhone: String = "",
    val ticketCode: String = "",    // e.g., "NK-58291-MPESA"
    val purchaseTime: Long = 0L     // System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "ticketId" to ticketId,
        "eventId" to eventId,
        "buyerName" to buyerName,
        "buyerPhone" to buyerPhone,
        "ticketCode" to ticketCode,
        "purchaseTime" to purchaseTime
    )
}

