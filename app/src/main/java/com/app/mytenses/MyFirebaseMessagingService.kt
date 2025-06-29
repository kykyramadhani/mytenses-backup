package com.app.mytenses

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.mytenses.R
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.text.SimpleDateFormat
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "Belajar yuk!"
        val body = remoteMessage.notification?.body ?: "Jangan lupa latihan hari ini!"
        showNotification(applicationContext, title, body)

        // Simpan ke history
        saveToHistory(applicationContext, title, body)

        // Kirim broadcast agar NotificationFragment refresh
        val intent = Intent("com.app.mytenses.NOTIF_HISTORY_UPDATED")
        sendBroadcast(intent)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        android.util.Log.d("FCM", "New token: $token")

        // simpan ke SharedPreferences
        val prefs = getSharedPreferences("MyTensesPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()

        // Ambil userId dari SharedPreferences
        val userId = prefs.getString("user_id", null)

        if (userId != null) {
            // Update ke Firebase Realtime Database
            val db = FirebaseDatabase.getInstance().getReference("users").child(userId)
            db.child("fcm_token").setValue(token)
                .addOnSuccessListener {
                    android.util.Log.d("FCM", "Token saved to Realtime Database for userId=$userId")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("FCM", "Failed to save token: ${e.message}")
                }
        } else {
            android.util.Log.w("FCM", "User ID not found, token only saved locally")
        }
    }

    private fun showNotification(context: Context, title: String, body: String) {
        val channelId = "daily_channel"
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notif)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Daily Notification", NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            with(NotificationManagerCompat.from(context)) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        } else {
            android.util.Log.w("FCM", "POST_NOTIFICATIONS permission not granted")
        }
    }

    private fun saveToHistory(context: Context, title: String, subtitle: String) {
        val prefs = context.getSharedPreferences("NotifHistory", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val set = prefs.getStringSet("data", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val dateTime = sdf.format(Date())
        val (date, time) = dateTime.split(" ")
        set.add("$title|$subtitle|$date|$time")

        editor.putStringSet("data", set)
        editor.apply()
    }
}
