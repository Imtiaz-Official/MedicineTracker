package com.example.medicinetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Serializable
enum class DurationUnit(val displayName: String) {
    DAYS("Days"),
    WEEKS("Weeks"),
    MONTHS("Months")
}

@Serializable
data class Medicine(
    val id: Long = 0,
    val name: String,
    val type: String,
    val dosage: String,
    val startDateString: String, // Stored as ISO string for easy serialization
    val durationValue: Int,
    val durationUnit: DurationUnit,
    val frequency: FrequencyType,
    val daysOfWeek: List<DayOfWeek>? = null,
    val timesPerDayStrings: List<String> // Stored as ISO strings
) {
    val startDate: LocalDate get() = LocalDate.parse(startDateString)
    val timesPerDay: List<LocalTime> get() = timesPerDayStrings.map { LocalTime.parse(it) }
}

@Serializable
data class DoseRecord(
    val medicineId: Long,
    val medicineName: String,
    val dateTimeString: String,
    val status: String // "TAKEN", "SKIPPED"
)
