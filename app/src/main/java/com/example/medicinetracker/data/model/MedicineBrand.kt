package com.example.medicinetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicine_brands")
data class MedicineBrand(
    @PrimaryKey val id: Long,
    val name: String,
    val dosageForm: String,
    val generic: String,
    val strength: String,
    val manufacturer: String,
    val genericId: Long? = null
)
