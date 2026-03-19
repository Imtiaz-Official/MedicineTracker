package com.example.medicinetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generic_info")
data class GenericInfo(
    @PrimaryKey val id: Long,
    val name: String,
    val indication: String?,
    val therapeuticClass: String?,
    val pharmacology: String?,
    val dosage: String?,
    val administration: String?,
    val interaction: String?,
    val contraindications: String?,
    val sideEffects: String?,
    val pregnancyLactation: String?,
    val precautions: String?,
    val storage: String?
)
