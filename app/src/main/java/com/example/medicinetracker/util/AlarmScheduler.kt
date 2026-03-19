package com.example.medicinetracker.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.medicinetracker.data.model.Medicine
import com.example.medicinetracker.data.model.FrequencyType
import com.example.medicinetracker.receiver.MedicineAlarmReceiver
import java.time.LocalDateTime
import java.time.ZoneId

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(medicine: Medicine) {
        if (medicine.frequency == FrequencyType.AS_NEEDED) return

        val zoneId = ZoneId.systemDefault()
        val now = LocalDateTime.now()
        
        medicine.timesPerDay.forEachIndexed { index, time ->
            val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
                putExtra("MEDICINE_NAME", medicine.name)
                putExtra("DOSAGE", medicine.dosage)
                putExtra("MEDICINE_ID", medicine.id) // Useful for rescheduling
            }

            // Robust Unique ID for each time of day for this medicine
            val requestCode = medicine.id.hashCode() * 31 + index
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )

            var alarmTime = now.with(time)
            
            // If the time has already passed today, start from tomorrow
            if (alarmTime.isBefore(now)) {
                alarmTime = alarmTime.plusDays(1)
            }

            val finalAlarmTime = when (medicine.frequency) {
                FrequencyType.DAILY -> alarmTime
                FrequencyType.WEEKLY, FrequencyType.SPECIFIC_DAYS -> {
                    if (medicine.daysOfWeek.isNullOrEmpty()) {
                        alarmTime
                    } else {
                        // Find the next day in the list starting from alarmTime
                        var next = alarmTime
                        var daysTried = 0
                        while (!medicine.daysOfWeek!!.contains(next.dayOfWeek) && daysTried < 8) {
                            next = next.plusDays(1)
                            daysTried++
                        }
                        next
                    }
                }
                else -> alarmTime
            }

            val epochMillis = finalAlarmTime.atZone(zoneId).toInstant().toEpochMilli()

            val alarmClockInfo = AlarmManager.AlarmClockInfo(epochMillis, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        }
    }

    fun cancelAlarms(medicine: Medicine) {
        medicine.timesPerDay.forEachIndexed { index, _ ->
            val intent = Intent(context, MedicineAlarmReceiver::class.java)
            val requestCode = medicine.id.hashCode() * 31 + index
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
