package com.example.medicinetracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medicinetracker.data.MedicineRepository
import com.example.medicinetracker.data.model.Medicine
import com.example.medicinetracker.data.model.MedicineBrand
import com.example.medicinetracker.data.model.MedicineSuggestion
import com.example.medicinetracker.data.model.DosageForm
import com.example.medicinetracker.util.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MedicineViewModel(
    application: Application,
    private val repository: MedicineRepository
) : AndroidViewModel(application) {

    private val alarmScheduler = AlarmScheduler(application)

    private val _suggestions = MutableStateFlow<List<MedicineSuggestion>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    private val _dosageForms = MutableStateFlow<List<DosageForm>>(emptyList())
    val dosageForms = _dosageForms.asStateFlow()

    init {
        loadDosageForms()
        populateBrands()
    }

    private fun loadDosageForms() {
        viewModelScope.launch {
            try {
                val jsonString = getApplication<Application>().assets.open("dosage_forms.json").bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                val list = mutableListOf<DosageForm>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(DosageForm(obj.getInt("id"), obj.getString("name")))
                }
                _dosageForms.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
private fun populateBrands() {
    viewModelScope.launch {
        try {
            val count = withContext(Dispatchers.IO) { repository.getBrandCount() }
            // We converted ~21k, so if we have less than 20k, let's re-populate or initial populate
            if (count > 20000) return@launch

            val jsonString = getApplication<Application>().assets.open("medicine_brands.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            val total = jsonArray.length()
            val chunkSize = 1000

            withContext(Dispatchers.IO) {
                for (i in 0 until total step chunkSize) {
                    val list = mutableListOf<MedicineBrand>()
                    val end = if (i + chunkSize > total) total else i + chunkSize
                    for (j in i until end) {
                        val obj = jsonArray.getJSONObject(j)
                        list.add(MedicineBrand(
                            id = obj.getLong("id"),
                            name = obj.getString("name"),
                            dosageForm = obj.getString("dosageForm"),
                            generic = obj.getString("generic"),
                            strength = obj.getString("strength"),
                            manufacturer = obj.getString("manufacturer")
                        ))
                    }
                    repository.insertBrands(list)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

    private var searchJob: kotlinx.coroutines.Job? = null

    fun searchMedicine(query: String) {
        searchJob?.cancel()
        if (query.length < 2) {
            _suggestions.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            val localResults = withContext(Dispatchers.IO) {
                repository.searchBrands(query).map { 
                    MedicineSuggestion(
                        name = it.name,
                        dosageForm = it.dosageForm,
                        generic = it.generic,
                        strength = it.strength,
                        isLocal = true
                    )
                }
            }
            _suggestions.value = localResults
        }
    }

    val allMedicines: StateFlow<List<Medicine>> = repository.allMedicines
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val doseHistory: StateFlow<List<com.example.medicinetracker.data.model.DoseRecord>> = repository.doseHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertAndSchedule(medicine: Medicine) = viewModelScope.launch {
        repository.insert(medicine)
        alarmScheduler.scheduleAlarm(medicine)
    }

    fun delete(medicine: Medicine) = viewModelScope.launch {
        repository.delete(medicine)
        alarmScheduler.cancelAlarms(medicine)
    }

    fun logDose(medicine: Medicine, status: String) = viewModelScope.launch {
        val record = com.example.medicinetracker.data.model.DoseRecord(
            medicineId = medicine.id,
            medicineName = medicine.name,
            dateTimeString = java.time.LocalDateTime.now().toString(),
            status = status
        )
        repository.saveDoseRecord(record)
    }

    fun deleteDoseRecord(record: com.example.medicinetracker.data.model.DoseRecord) = viewModelScope.launch {
        repository.deleteDoseRecord(record)
    }
}

class MedicineViewModelFactory(
    private val application: Application,
    private val repository: MedicineRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicineViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
