package com.sc.eventnotifyke.models

// 1. Enum class: static values that should not change
enum class UserRole { USER, ADMIN }

// 2. Data class representing the User Profile
data class UserProfile(
    val uid: String = "",
    val fullname: String = "",
    val email: String = "",
    val phone: String = "",
    val estate: String = "", // e.g., "Kilimani", "Roysambu", "Juja"
    val role: String = "user"
) {
    // Fixed: changed "fullName" to "fullname" to prevent case-mismatch bugs
    fun toMap(): Map<String, Any> = mapOf(
        "uid" to uid,
        "fullname" to fullname,
        "email" to email,
        "phone" to phone,
        "estate" to estate,
        "role" to role
    )

    fun userRole(): UserRole =
        if (role == "admin") UserRole.ADMIN else UserRole.USER
}
