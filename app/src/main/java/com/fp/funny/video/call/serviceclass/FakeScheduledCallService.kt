package com.fp.funny.video.call.serviceclass

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.fp.funny.video.call.IncomingVoiceCallActivity
import com.fp.funny.video.call.ScheduleACallActivity
import com.fp.funny.video.call.broadcastreceiver.FakeScheduledCallReceiver
import com.fp.funny.video.call.AcceptCallActivity
import com.fp.funny.video.call.R

class FakeScheduledCallService : Service() {

    private var isPlaying = false
    private lateinit var ringtone: Ringtone
    private val handler = Handler(Looper.getMainLooper())
    private val binder = FakeCallBinder()
    private var overlayView: View? = null
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var imageResId: String
   /* private var nameOfCharacter: String? = null*/
//    private var celebrityFullImage: String? = null
    private lateinit var nameOfTheCharacter: String


    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    inner class FakeCallBinder : Binder() {
        fun getService(): FakeScheduledCallService = this@FakeScheduledCallService
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val callerName = intent?.getStringExtra("CALLER_NAME")
        val ringtoneResId = intent?.getIntExtra("ringtoneResId", -1)
        /*val callerNumber = intent?.getStringExtra("CALLER_NUMBER")*/
        val triggerTime = intent?.getLongExtra("TRIGGER_TIME", 0L) ?: 0L
        val ringtoneId = intent?.getIntExtra("RINGTONE_ID", R.raw.first_ringtone)
        imageResId = intent?.getStringExtra("imageResId").toString()
         nameOfTheCharacter = intent?.getStringExtra("nameResId").toString()



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission()) {
                showNotificationPermissionDialog()
                return START_STICKY
            }
        }


        val notification = buildNotification()
        startForeground(1, notification)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "FakeScheduledCallService::WakeLock"
        )
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)

        if (triggerTime > System.currentTimeMillis()) {
            if (ringtoneResId != null) {
                scheduleAlarm(triggerTime, callerName)
            }
        } else {
            if (ringtoneId != null) {
                startFakeCall(callerName, ringtoneId)
            }
        }

        stopSelf()

        return START_STICKY
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun hasNotificationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showNotificationPermissionDialog() {
        val intent = Intent(this, ScheduleACallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("SHOW_PERMISSION_DIALOG", true)
        }
        startActivity(intent)
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleAlarm(triggerTime: Long, callerName: String?) {
        val intent = Intent(this, FakeScheduledCallReceiver::class.java).apply {
            putExtra("CALLER_NAME", callerName)
            /*putExtra("CALLER_NUMBER", callerNumber)*/
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun startFakeCall(callerName: String?, ringtoneId: Int) {
        if (callerName != null && !isPlaying) {
            isPlaying = true
            val ringtoneUri = Uri.parse("android.resource://${packageName}/$ringtoneId")
            playRingtone(ringtoneUri)
            showIncomingCallActivity(callerName, ringtoneId)
            if (Settings.canDrawOverlays(this)) {
                showOverlay(callerName)
            }
        }
    }


    private fun showIncomingCallActivity(callerName: String, ringtoneId: Int) {
        val intent = Intent(this, IncomingVoiceCallActivity::class.java).apply {
            putExtra("CALLER_NAME", callerName)
            putExtra("IS_FROM_SERVICE", true)
            putExtra("RINGTONE_ID", ringtoneId) // Pass the ringtone ID
            /*putExtra("CALLER_NUMBER", callerNumber)*/
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
        startActivity(intent)
    }

    @SuppressLint("InflateParams")
    private fun showOverlay(callerName: String) {
        if (!Settings.canDrawOverlays(this)) {
            return
        }

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        overlayView = inflater.inflate(R.layout.activity_incoming_voice_call, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(overlayView, params)

        val tvCallerName = overlayView?.findViewById<TextView>(R.id.name_of_the_character_tv)
       /* val tvCallerNumber = overlayView?.findViewById<TextView>(R.id.caller_number_tv)*/
        val btnAccept = overlayView?.findViewById<ImageView>(R.id.accept_icon)
        val btnDecline = overlayView?.findViewById<ImageView>(R.id.decline_icon)

        tvCallerName?.text = callerName
        /*tvCallerNumber?.text = callerNumber*/


        btnAccept?.setOnClickListener {
            stopCall()
            // Handle call acceptance
            Toast.makeText(this, "Call Accepted", Toast.LENGTH_SHORT).show()
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(overlayView)
            val intent = Intent(this, AcceptCallActivity::class.java).apply {
                putExtra("imageResId", imageResId)
                putExtra("nameResId", nameOfTheCharacter)

                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)

        }

        btnDecline?.setOnClickListener {
            stopCall()
            Toast.makeText(this, "Call declined", Toast.LENGTH_LONG).show()
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(overlayView)
        }
        }

    private fun buildNotification(): Notification {
        val channelId = "fake_call_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Incoming Fake Voice Call")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Fake Call Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

        return notificationBuilder.build()
    }


    private fun playRingtone(uri: Uri) {
        ringtone = RingtoneManager.getRingtone(applicationContext, uri)
        ringtone.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)

        if (::ringtone.isInitialized && ringtone.isPlaying) {
            ringtone.stop()
        }

        if (overlayView != null && overlayView?.isAttachedToWindow == true) {
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(overlayView)
        }

        if (::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    private fun stopCall() {
        if (::ringtone.isInitialized && ringtone.isPlaying) {
            ringtone.stop()
        }

        overlayView?.let {
            if (it.isAttachedToWindow) {
                val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
                windowManager.removeView(it)
            }
        }

        if (::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
        }

        stopSelf()
    }

    companion object {
        const val REQUEST_OVERLAY_PERMISSION = 1234
    }

}