package com.example.medicinetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.medicinetracker.data.model.Medicine
import com.example.medicinetracker.data.model.MedicineBrand

@Database(entities = [MedicineBrand::class], version = 7, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MedicineDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao

    companion object {
        @Volatile
        private var INSTANCE: MedicineDatabase? = null

        fun getDatabase(context: Context): MedicineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicineDatabase::class.java,
                    "medicine_database"
                )
                .fallbackToDestructiveMigration() // For development, reset DB on version change
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
