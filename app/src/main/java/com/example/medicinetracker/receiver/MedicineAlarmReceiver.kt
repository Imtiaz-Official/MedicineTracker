package com.example.medicinetracker.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.graphics.Typeface
import com.example.medicinetracker.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import android.app.PendingIntent

class MedicineAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val medicineId = intent.getLongExtra("MEDICINE_ID", -1L)
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", -1)

        if (action == "ACTION_TAKE" || action == "ACTION_SKIP") {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationId != -1) {
                notificationManager.cancel(notificationId)
            }
            
            // Stop the alarm service
            context.stopService(Intent(context, com.example.medicinetracker.service.MedicineAlarmService::class.java))
            
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefsManager = com.example.medicinetracker.data.local.MedicinePrefsManager(context)
                    val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Medicine"
                    val status = if (action == "ACTION_TAKE") "TAKEN" else "SKIPPED"
                    
                    val record = com.example.medicinetracker.data.model.DoseRecord(
                        medicineId = medicineId,
                        medicineName = medicineName,
                        dateTimeString = java.time.LocalDateTime.now().toString(),
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

        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Medicine"
        val dosage = intent.getStringExtra("DOSAGE") ?: ""

        showNotification(context, medicineName, dosage, medicineId)

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

    private fun showNotification(context: Context, name: String, dosage: String, medicineId: Long) {
        val channelId = "medicine_reminder_channel_v2"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        
        // Start the foreground service for continuous alarm sound and vibration
        val serviceIntent = Intent(context, com.example.medicinetracker.service.MedicineAlarmService::class.java).apply {
            putExtra("MEDICINE_ID", medicineId)
            putExtra("MEDICINE_NAME", name)
            putExtra("DOSAGE", dosage)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to take your medicine"
                enableLights(true)
                enableVibration(true)
                setSound(alarmSound, AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
            }
            notificationManager.createNotificationChannel(channel)
        }

        val takeIntent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            action = "ACTION_TAKE"
            putExtra("MEDICINE_ID", medicineId)
            putExtra("MEDICINE_NAME", name)
            putExtra("NOTIFICATION_ID", notificationId)
        }
        val takePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1,
            takeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val skipIntent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            action = "ACTION_SKIP"
            putExtra("MEDICINE_ID", medicineId)
            putExtra("MEDICINE_NAME", name)
            putExtra("NOTIFICATION_ID", notificationId)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to open the app when the notification is clicked
        val openAppIntent = Intent(context, com.example.medicinetracker.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 3,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val content = SpannableStringBuilder("Please take $dosage of $name")
        content.setSpan(StyleSpan(Typeface.BOLD), content.length - name.length, content.length, 0)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time for your medicine!")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .addAction(android.R.drawable.ic_menu_edit, "Take", takePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Skip", skipPendingIntent)
            .setFullScreenIntent(openAppPendingIntent, true) // Wakes screen on off
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
