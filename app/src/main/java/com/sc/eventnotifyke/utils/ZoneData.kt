package com.sc.eventnotifyke.utils

val zoneNeighborhoods = mapOf(
    "Westlands" to listOf(
        "Westlands", "Parklands", "Gigiri", "Muthaiga", "Spring Valley", "Loresho", "Kitisuru"
    ),
    "Kilimani & Lavington Axis" to listOf(
        "Kilimani", "Kileleshwa", "Lavington", "Riverside"
    ),
    "Karen & Lang'ata (Southern)" to listOf(
        "Karen", "Lang'ata", "Hardy", "Madaraka", "South C"
    ),
    "CBD & Immediate Environs" to listOf(
        "Nairobi CBD", "Ngara", "Upper Hill", "South B"
    ),
    "Eastern Corridor" to listOf(
        "Embakasi", "Donholm", "Umoja", "Kayole", "Ruai", "Komarock", "Utawala"
    ),
    "Thika Road & Northern Corridor" to listOf(
        "Kasarani", "Roysambu", "Githurai", "Zimmerman", "Kahawa", "Mwiki"
    ),
    "Ngong Road Axis" to listOf(
        "Ngong", "Rongai", "Kiserian", "Riruta", "Kawangware", "Waithaka"
    )
)

// flat list of all neighborhoods for any screen that needs them
val allNeighborhoods: List<String> =
    zoneNeighborhoods.values.flatten().distinct()