package com.example.medicinetracker.data

import com.example.medicinetracker.data.local.MedicineDao
import com.example.medicinetracker.data.local.MedicinePrefsManager
import com.example.medicinetracker.data.model.Medicine
import com.example.medicinetracker.data.model.MedicineBrand
import kotlinx.coroutines.flow.Flow

class MedicineRepository(
    private val medicineDao: MedicineDao,
    private val prefsManager: MedicinePrefsManager
) {
    val allMedicines: Flow<List<Medicine>> = prefsManager.medicines
    val doseHistory: Flow<List<com.example.medicinetracker.data.model.DoseRecord>> = prefsManager.history

    suspend fun insert(medicine: Medicine) {
        prefsManager.saveMedicine(medicine)
    }

    suspend fun delete(medicine: Medicine) {
        prefsManager.deleteMedicine(medicine)
    }

    suspend fun saveDoseRecord(record: com.example.medicinetracker.data.model.DoseRecord) {
        prefsManager.saveDoseRecord(record)
    }

    suspend fun deleteDoseRecord(record: com.example.medicinetracker.data.model.DoseRecord) {
        prefsManager.deleteDoseRecord(record)
    }

    suspend fun getMedicineById(id: Long): Medicine? {
        return prefsManager.medicines.value.find { it.id == id }
    }

    suspend fun searchBrands(query: String): List<MedicineBrand> {
        return medicineDao.searchBrands(query)
    }

    suspend fun getBrandCount(): Int {
        return medicineDao.getBrandCount()
    }

    suspend fun insertBrands(brands: List<MedicineBrand>) {
        medicineDao.insertBrands(brands)
    }

    suspend fun insertGenerics(generics: List<com.example.medicinetracker.data.model.GenericInfo>) {
        medicineDao.insertGenerics(generics)
    }

    suspend fun getGenericInfoById(id: Long): com.example.medicinetracker.data.model.GenericInfo? {
        return medicineDao.getGenericInfoById(id)
    }

    suspend fun getGenericInfoByName(name: String): com.example.medicinetracker.data.model.GenericInfo? {
        return medicineDao.getGenericInfoByName(name)
    }

    suspend fun getAlternateBrands(genericName: String, currentBrandName: String): List<MedicineBrand> {
        return medicineDao.getAlternateBrands(genericName, currentBrandName)
    }
}
