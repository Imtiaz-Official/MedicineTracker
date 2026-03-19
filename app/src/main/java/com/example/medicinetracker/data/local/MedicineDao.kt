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
                WHEN name LIKE :query || '%' THEN 1 
                WHEN generic LIKE :query || '%' THEN 2
                ELSE 3 
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
}
