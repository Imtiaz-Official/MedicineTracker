package com.example.medicinetracker.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class FrequencyType(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    AS_NEEDED("As Needed"),
    SPECIFIC_DAYS("Specific Days")
}
