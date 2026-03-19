package com.example.medicinetracker.data.model

data class MedicineSuggestion(
    val name: String,
    val dosageForm: String? = null,
    val generic: String? = null,
    val strength: String? = null,
    val isLocal: Boolean = false
)
