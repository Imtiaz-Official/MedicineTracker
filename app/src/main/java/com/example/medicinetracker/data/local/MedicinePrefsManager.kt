package com.example.medicinetracker.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.medicinetracker.data.model.Medicine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MedicinePrefsManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "encrypted_medicines",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _medicines = MutableStateFlow<List<Medicine>>(loadMedicines())
    val medicines = _medicines.asStateFlow()

    private val _history = MutableStateFlow<List<com.example.medicinetracker.data.model.DoseRecord>>(loadHistory())
    val history = _history.asStateFlow()

    private val preferenceChangeListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            "medicine_list" -> _medicines.value = loadMedicines()
            "dose_history" -> _history.value = loadHistory()
        }
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    private fun loadMedicines(): List<Medicine> {
        val json = sharedPreferences.getString("medicine_list", null) ?: return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadHistory(): List<com.example.medicinetracker.data.model.DoseRecord> {
        val json = sharedPreferences.getString("dose_history", null) ?: return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveDoseRecord(record: com.example.medicinetracker.data.model.DoseRecord) {
        val currentHistory = _history.value.toMutableList()
        currentHistory.add(0, record) // Newest first
        
        // Keep only last 500 records to prevent pref bloat
        if (currentHistory.size > 500) {
            currentHistory.removeAt(currentHistory.size - 1)
        }

        saveHistory(currentHistory)
    }

    fun deleteDoseRecord(record: com.example.medicinetracker.data.model.DoseRecord) {
        val currentHistory = _history.value.toMutableList()
        currentHistory.removeAll { it.dateTimeString == record.dateTimeString && it.medicineId == record.medicineId }
        saveHistory(currentHistory)
    }

    private fun saveHistory(history: List<com.example.medicinetracker.data.model.DoseRecord>) {
        val json = Json.encodeToString(history)
        sharedPreferences.edit().putString("dose_history", json).apply()
        _history.value = history
    }

    fun saveMedicine(medicine: Medicine) {
        val currentList = _medicines.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == medicine.id }
        
        val medicineToSave = if (medicine.id == 0L) {
            medicine.copy(id = System.currentTimeMillis())
        } else {
            medicine
        }

        if (index != -1) {
            currentList[index] = medicineToSave
        } else {
            currentList.add(medicineToSave)
        }

        persist(currentList)
    }

    fun deleteMedicine(medicine: Medicine) {
        val currentList = _medicines.value.toMutableList()
        currentList.removeAll { it.id == medicine.id }
        persist(currentList)
    }

    private fun persist(list: List<Medicine>) {
        val json = Json.encodeToString(list)
        sharedPreferences.edit().putString("medicine_list", json).apply()
        _medicines.value = list
    }
}
