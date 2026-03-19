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
        ORDER BY 
            CASE 
                WHEN name LIKE :query || '%' THEN 1 
                ELSE 2 
            END, 
            name ASC 
        LIMIT 20
    """)
    suspend fun searchBrands(query: String): List<MedicineBrand>

    @Query("SELECT COUNT(*) FROM medicine_brands")
    suspend fun getBrandCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrands(brands: List<MedicineBrand>)
}
