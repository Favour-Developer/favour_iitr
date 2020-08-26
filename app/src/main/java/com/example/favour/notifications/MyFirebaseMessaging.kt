package com.example.favour.notifications

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.example.favour.MainActivity
import com.example.favour.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class MyFirebaseMessaging : FirebaseMessagingService() {
    override fun onMessageReceived(mRemoteMessage: RemoteMessage) {
        super.onMessageReceived(mRemoteMessage)

        val sented = mRemoteMessage.data["sented"]
        val user = mRemoteMessage.data["user"]
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val isScheduled = mRemoteMessage.data["isScheduled"]?.toBoolean()

        if (firebaseUser != null && sented == firebaseUser.uid) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendNotificationOreo(mRemoteMessage)
            } else sendNotification(mRemoteMessage)
            val data = Data(
                user!!,
                R.mipmap.app_icon,
                mRemoteMessage.data["body"]!!,
                mRemoteMessage.data["title"]!!,
                sented
            )
            val database = FirebaseDatabase.getInstance().reference
            database.child("notifications")
                .child(FirebaseAuth.getInstance().uid.toString()).push().setValue(data)
        }


//        if (isScheduled!!) scheduleAlarm(
//            mRemoteMessage.data["scheduledTime"],
//            mRemoteMessage.data["title"],
//            mRemoteMessage.data["body"]
//        )


    }

//    private fun scheduleAlarm(time: String?, title: String?, body: String?) {
//        val alarmMgr = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val alarmIntent =
//            Intent(applicationContext, RECEIVER::class.java).let { intent ->
//                intent.putExtra(, title)
//                intent.putExtra(NOTIFICATION_MESSAGE, message)
//                PendingIntent.getBroadcast(applicationContext, 0, intent, 0)
//            }
//
//        // Parse Schedule time
//        val scheduledTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//            .parse(time)
//
//        scheduledTime?.let {
//            // With set(), it'll set non repeating one time alarm.
//            alarmMgr.set(
//                AlarmManager.RTC_WAKEUP,
//                it.time,
//                alarmIntent
//            )
//        }
//    }

    private fun sendNotification(mRemoteMessage: RemoteMessage) {
        val user = mRemoteMessage.data["user"]
        val icon = mRemoteMessage.data["icon"]
        val title = mRemoteMessage.data["title"]
        val body = mRemoteMessage.data["body"]

        val notification = mRemoteMessage.notification
        var j = user!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, MainActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userId", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this)
            .setSmallIcon(icon!!.toInt()).setContentTitle(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentIntent(pendingIntent)

        val noti = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val i = 0
        if (j > 0) j = i

        noti.notify(i, builder.build())
    }

    private fun sendNotificationOreo(mRemoteMessage: RemoteMessage) {
        val user = mRemoteMessage.data["user"]
        val icon = mRemoteMessage.data["icon"]
        val title = mRemoteMessage.data["title"]
        val body = mRemoteMessage.data["body"]

        val notification = mRemoteMessage.notification
        var j = user!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, MainActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userId", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val oreoNotification = OreoNotification(this)
        val builder: Notification.Builder =
            oreoNotification.getOreoNotification(title, body, pendingIntent, defaultSound, icon)
        val i = 0
        if (j > 0) j = i

        oreoNotification.getManager!!.notify(i, builder.build())

    }
}