package com.example.medicinetracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.medicinetracker.data.model.MedicineBrand

@Dao
interface MedicineDao {
    // Reference Data Methods
    @Query("""
        SELECT * FROM medicine_brands 
        WHERE name LIKE '%' || :query || '%' 
           OR generic LIKE '%' || :query || '%'
        ORDER BY 
            CASE 
                WHEN LOWER(name) = LOWER(:query) THEN 1
                WHEN LOWER(name) LIKE LOWER(:query) || ' %' THEN 2
                WHEN LOWER(name) LIKE LOWER(:query) || '%' THEN 3
                WHEN LOWER(generic) = LOWER(:query) THEN 4
                WHEN LOWER(generic) LIKE LOWER(:query) || '%' THEN 5
                ELSE 6
            END, 
            name ASC 
        LIMIT 50
    """)
    suspend fun searchBrands(query: String): List<MedicineBrand>

    @Query("SELECT COUNT(*) FROM medicine_brands")
    suspend fun getBrandCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrands(brands: List<MedicineBrand>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenerics(generics: List<com.example.medicinetracker.data.model.GenericInfo>)

    @Query("SELECT * FROM generic_info WHERE id = :id")
    suspend fun getGenericInfoById(id: Long): com.example.medicinetracker.data.model.GenericInfo?

    @Query("SELECT * FROM generic_info WHERE name = :name LIMIT 1")
    suspend fun getGenericInfoByName(name: String): com.example.medicinetracker.data.model.GenericInfo?

    @Query("SELECT * FROM medicine_brands WHERE generic = :genericName AND name != :currentBrandName LIMIT 20")
    suspend fun getAlternateBrands(genericName: String, currentBrandName: String): List<MedicineBrand>
}
