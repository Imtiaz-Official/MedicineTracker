package com.example.medicinetracker.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.medicinetracker.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MedicineAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val medicineId = intent.getLongExtra("MEDICINE_ID", -1L)
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", -1)

        if (action == "ACTION_TAKE" || action == "ACTION_SKIP") {
            // Stop the alarm service immediately
            context.stopService(Intent(context, com.example.medicinetracker.service.MedicineAlarmService::class.java))
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationId != -1) {
                notificationManager.cancel(notificationId)
            }
            
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefsManager = com.example.medicinetracker.data.local.MedicinePrefsManager(context)
                    val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Medicine"
                    val status = if (action == "ACTION_TAKE") "TAKEN" else "SKIPPED"
                    
                    val record = com.example.medicinetracker.data.model.DoseRecord(
                        medicineId = medicineId,
                        medicineName = medicineName,
                        dateTimeString = LocalDateTime.now().toString(),
                        status = status
                    )
                    prefsManager.saveDoseRecord(record)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        // --- Standard Alarm Trigger ---
        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Medicine"
        val dosage = intent.getStringExtra("DOSAGE") ?: ""

        // 1. Start the foreground service for continuous alarm sound and vibration
        val serviceIntent = Intent(context, com.example.medicinetracker.service.MedicineAlarmService::class.java).apply {
            putExtra("MEDICINE_ID", medicineId)
            putExtra("MEDICINE_NAME", medicineName)
            putExtra("DOSAGE", dosage)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // 2. Schedule the next alarm occurrence
        if (medicineId != -1L) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefsManager = com.example.medicinetracker.data.local.MedicinePrefsManager(context)
                    val medicine = prefsManager.medicines.value.find { it.id == medicineId }
                    if (medicine != null) {
                        AlarmScheduler(context).scheduleAlarm(medicine)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
