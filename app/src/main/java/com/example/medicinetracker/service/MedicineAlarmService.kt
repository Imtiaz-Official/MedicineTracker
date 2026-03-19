package com.example.medicinetracker.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.graphics.Typeface
import com.example.medicinetracker.MainActivity
import com.example.medicinetracker.receiver.MedicineAlarmReceiver

class MedicineAlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val medicineId = intent?.getLongExtra("MEDICINE_ID", -1L) ?: -1L
        val medicineName = intent?.getStringExtra("MEDICINE_NAME") ?: "Medicine"
        val dosage = intent?.getStringExtra("DOSAGE") ?: ""

        val action = intent?.action
        if (action == "STOP_ALARM") {
            stopSelf()
            return START_NOT_STICKY
        }

        showForegroundNotification(medicineName, dosage, medicineId)
        startAlarm()
        startVibration()

        return START_STICKY
    }

    private fun showForegroundNotification(name: String, dosage: String, medicineId: Long) {
        val channelId = "medicine_alarm_service_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Active Medicine Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null) // Sound handled by MediaPlayer
                enableVibration(false) // Vibration handled by Vibrator
            }
            notificationManager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, MedicineAlarmService::class.java).apply {
            action = "STOP_ALARM"
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val takeIntent = Intent(this, MedicineAlarmReceiver::class.java).apply {
            action = "ACTION_TAKE"
            putExtra("MEDICINE_ID", medicineId)
            putExtra("MEDICINE_NAME", name)
        }
        val takePendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            takeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val skipIntent = Intent(this, MedicineAlarmReceiver::class.java).apply {
            action = "ACTION_SKIP"
            putExtra("MEDICINE_ID", medicineId)
            putExtra("MEDICINE_NAME", name)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            this,
            3,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            2,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = SpannableStringBuilder("Medicine Reminder: $name")
        title.setSpan(StyleSpan(Typeface.BOLD), 19, 19 + name.length, 0)

        val content = SpannableStringBuilder("It's time to take $dosage of $name")
        content.setSpan(StyleSpan(Typeface.BOLD), content.length - name.length, content.length, 0)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(openAppPendingIntent, true)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_menu_edit, "Take", takePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Skip", skipPendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "Dismiss", stopPendingIntent)
            .build()

        startForeground(1001, notification)
    }

    private fun startAlarm() {
        try {
            val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@MedicineAlarmService, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 500, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
    }
}
