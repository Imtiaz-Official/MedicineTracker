package com.example.medicinetracker.ui

import com.example.medicinetracker.util.MedexCrawler
import com.example.medicinetracker.util.AssetEncryptionUtil
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
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class MedicineViewModel(
    application: Application,
    private val repository: MedicineRepository
) : AndroidViewModel(application) {

    private val alarmScheduler = AlarmScheduler(application)

    private val _suggestions = MutableStateFlow<List<MedicineSuggestion>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    private val _searchResults = MutableStateFlow<List<MedicineBrand>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _selectedGenericInfo = MutableStateFlow<com.example.medicinetracker.data.model.GenericInfo?>(null)
    val selectedGenericInfo = _selectedGenericInfo.asStateFlow()

    private val _isLoadingGenericInfo = MutableStateFlow(false)
    val isLoadingGenericInfo = _isLoadingGenericInfo.asStateFlow()

    private val _alternateBrands = MutableStateFlow<List<MedicineBrand>>(emptyList())
    val alternateBrands = _alternateBrands.asStateFlow()

    private val _liveAlternateBrands = MutableStateFlow<List<MedicineBrand>>(emptyList())
    val liveAlternateBrands = _liveAlternateBrands.asStateFlow()

    private val _isLiveLoading = MutableStateFlow(false)
    val isLiveLoading = _isLiveLoading.asStateFlow()

    private val _dosageForms = MutableStateFlow<List<DosageForm>>(emptyList())
    val dosageForms = _dosageForms.asStateFlow()

    init {
        loadDosageForms()
        populateBrands()
    }

    private fun loadDosageForms() {
        viewModelScope.launch {
            try {
                val jsonString = AssetEncryptionUtil.getDecryptedStream(getApplication(), "dosage_forms.json").bufferedReader().use { it.readText() }
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
                android.util.Log.d("MedicineViewModel", "Current brand count in DB: $count")
                if (count > 20000) {
                    android.util.Log.d("MedicineViewModel", "Database already populated.")
                    return@launch
                }

                android.util.Log.d("MedicineViewModel", "Starting brand population from encrypted assets...")

                // 1. Populate Generics first
                val genericsJson = AssetEncryptionUtil.getDecryptedStream(getApplication(), "medicine_generics.json").bufferedReader().use { it.readText() }
                val genericsArray = JSONArray(genericsJson)
                val genericsList = mutableListOf<com.example.medicinetracker.data.model.GenericInfo>()
                for (i in 0 until genericsArray.length()) {
                    val obj = genericsArray.getJSONObject(i)
                    genericsList.add(com.example.medicinetracker.data.model.GenericInfo(
                        id = obj.getLong("id"),
                        name = obj.getString("name"),
                        indication = obj.optString("indication").takeIf { it != "null" && it.isNotBlank() },
                        therapeuticClass = obj.optString("therapeuticClass").takeIf { it != "null" && it.isNotBlank() },
                        pharmacology = obj.optString("pharmacology").takeIf { it != "null" && it.isNotBlank() },
                        dosage = obj.optString("dosage").takeIf { it != "null" && it.isNotBlank() },
                        administration = obj.optString("administration").takeIf { it != "null" && it.isNotBlank() },
                        interaction = obj.optString("interaction").takeIf { it != "null" && it.isNotBlank() },
                        contraindications = obj.optString("contraindications").takeIf { it != "null" && it.isNotBlank() },
                        sideEffects = obj.optString("sideEffects").takeIf { it != "null" && it.isNotBlank() },
                        pregnancyLactation = obj.optString("pregnancyLactation").takeIf { it != "null" && it.isNotBlank() },
                        precautions = obj.optString("precautions").takeIf { it != "null" && it.isNotBlank() },
                        storage = obj.optString("storage").takeIf { it != "null" && it.isNotBlank() }
                    ))
                }
                withContext(Dispatchers.IO) { 
                    repository.insertGenerics(genericsList) 
                    android.util.Log.d("MedicineViewModel", "Populated ${genericsList.size} generics")
                }

                // 2. Populate Brands
                val brandsJson = AssetEncryptionUtil.getDecryptedStream(getApplication(), "medicine_brands.json").bufferedReader().use { it.readText() }
                val brandsArray = JSONArray(brandsJson)

            val total = brandsArray.length()
            val chunkSize = 1000

            withContext(Dispatchers.IO) {
                for (i in 0 until total step chunkSize) {
                    val list = mutableListOf<MedicineBrand>()
                    val end = if (i + chunkSize > total) total else i + chunkSize
                    for (j in i until end) {
                        val obj = brandsArray.getJSONObject(j)
                        
                        // FIX: Safely handle null genericId
                        val genericId = if (!obj.isNull("genericId")) obj.getLong("genericId") else null
                        
                        list.add(MedicineBrand(
                            id = obj.getLong("id"),
                            name = obj.getString("name"),
                            dosageForm = obj.getString("dosageForm"),
                            generic = obj.getString("generic"),
                            strength = obj.getString("strength"),
                            manufacturer = obj.getString("manufacturer"),
                            genericId = if (!obj.isNull("genericId")) obj.getLong("genericId") else null,
                            packageContainer = obj.optString("packageContainer").takeIf { it != "null" },
                            packageSize = obj.optString("packageSize").takeIf { it != "null" }
                        ))
                    }
                    repository.insertBrands(list)
                    if (end % 5000 == 0 || end == total) {
                        android.util.Log.d("MedicineViewModel", "Populated batch: $end / $total brands")
                    }
                }
                val finalCount = repository.getBrandCount()
                android.util.Log.d("MedicineViewModel", "Population complete. Total brands in DB: $finalCount")
            }
        } catch (e: Exception) {
            android.util.Log.e("MedicineViewModel", "Error populating brands", e)
        }
    }
}

    private var searchJob: kotlinx.coroutines.Job? = null
    private var dedicatedSearchJob: kotlinx.coroutines.Job? = null

    fun getGenericInfo(brand: MedicineBrand) {
        viewModelScope.launch {
            _isLoadingGenericInfo.value = true
            _isLiveLoading.value = true
            _selectedGenericInfo.value = null
            
            android.util.Log.d("MedicineViewModel", "Fetching generic info for brand: ${brand.name}")
            
            // 1. Try Live Medex Crawler FIRST
            android.util.Log.d("MedicineViewModel", "Attempting Medex crawl first...")
            var info = withContext(Dispatchers.IO) {
                MedexCrawler.fetchMonographFromMedex(brand.name, brand.generic)
            }

            // 2. Fallback to Local Database if Live fails
            if (info == null) {
                android.util.Log.d("MedicineViewModel", "Medex crawl failed or returned no data. Falling back to local...")
                _isLiveLoading.value = false
                info = withContext(Dispatchers.IO) {
                    if (brand.genericId != null) {
                        repository.getGenericInfoById(brand.genericId)
                    } else {
                        repository.getGenericInfoByName(brand.generic)
                    }
                }
                android.util.Log.d("MedicineViewModel", "Local fallback complete. Info found: ${info != null}")
            } else {
                android.util.Log.d("MedicineViewModel", "Successfully fetched LIVE info from Medex.")
            }

            _selectedGenericInfo.value = info
            _isLoadingGenericInfo.value = false
            _isLiveLoading.value = false

            // Fetch alternates in parallel
            viewModelScope.launch {
                val localAlternatesDeferred = async(Dispatchers.IO) {
                    repository.getAlternateBrands(brand.generic, brand.name)
                }
                val liveAlternatesDeferred = async(Dispatchers.IO) {
                    MedexCrawler.fetchAlternateBrandsFromMedex(brand.name)
                }

                _alternateBrands.value = localAlternatesDeferred.await()
                _liveAlternateBrands.value = liveAlternatesDeferred.await()
            }
        }
    }

    fun refreshFromMedex(brand: MedicineBrand) {
        viewModelScope.launch {
            _isLiveLoading.value = true
            android.util.Log.d("MedicineViewModel", "Manual refresh from Medex for ${brand.name}")
            val info = withContext(Dispatchers.IO) {
                MedexCrawler.fetchMonographFromMedex(brand.name, brand.generic)
            }
            if (info != null) {
                _selectedGenericInfo.value = info
            }
            
            // Also refresh live alternates
            val liveAlts = withContext(Dispatchers.IO) {
                MedexCrawler.fetchAlternateBrandsFromMedex(brand.name)
            }
            _liveAlternateBrands.value = liveAlts
            
            _isLiveLoading.value = false
        }
    }

    fun clearSelectedGenericInfo() {
        _selectedGenericInfo.value = null
        _isLoadingGenericInfo.value = false
        _alternateBrands.value = emptyList()
        _liveAlternateBrands.value = emptyList()
    }

    fun searchMedicine(query: String) {
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _suggestions.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            _isSearching.value = true
            kotlinx.coroutines.delay(300)
            
            // Search local and live in parallel for suggestions
            val localResultsDeferred = async(Dispatchers.IO) {
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
            
            val liveResultsDeferred = async(Dispatchers.IO) {
                MedexCrawler.searchBrandsFromMedex(query).map {
                    MedicineSuggestion(
                        name = it.name,
                        dosageForm = it.dosageForm,
                        generic = it.generic,
                        strength = it.strength,
                        isLocal = false
                    )
                }
            }

            val localResults = localResultsDeferred.await()
            val liveResults = liveResultsDeferred.await()
            
            // Merge suggestions, avoiding duplicates
            val combined = (localResults + liveResults).distinctBy { "${it.name}-${it.generic}-${it.strength}" }
            
            _suggestions.value = combined
            _isSearching.value = false
        }
    }

    fun performDedicatedSearch(query: String) {
        _searchQuery.value = query
        dedicatedSearchJob?.cancel()
        if (query.length < 2) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        dedicatedSearchJob = viewModelScope.launch {
            _isSearching.value = true
            kotlinx.coroutines.delay(300)
            
            // Search local database and Medex in parallel
            val localResultsDeferred = async(Dispatchers.IO) {
                repository.searchBrands(query)
            }
            
            val liveResultsDeferred = async(Dispatchers.IO) {
                MedexCrawler.searchBrandsFromMedex(query)
            }
            
            val localResults = localResultsDeferred.await()
            val liveResults = liveResultsDeferred.await()
            
            // Merge results, avoiding duplicates by name
            val combinedResults = (localResults + liveResults).distinctBy { "${it.name}-${it.generic}-${it.strength}" }
            
            android.util.Log.d("MedicineViewModel", "Search for '$query': ${localResults.size} local, ${liveResults.size} live. Combined: ${combinedResults.size}")
            
            _searchResults.value = combinedResults
            _isSearching.value = false
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
            initialValue = emptyValueList()
        )

    private fun <T> emptyValueList(): List<T> = emptyList()

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
