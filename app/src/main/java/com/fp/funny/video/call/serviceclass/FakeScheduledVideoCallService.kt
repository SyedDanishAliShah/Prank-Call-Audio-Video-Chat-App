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
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.fp.funny.video.call.AcceptVideoCallActivity
import com.fp.funny.video.call.IncomingVideoCallActivity
import com.fp.funny.video.call.MainActivity
import com.fp.funny.video.call.ScheduleACallActivity
import com.fp.funny.video.call.broadcastreceiver.FakeScheduledCallReceiver
import com.fp.funny.video.call.R

class FakeScheduledVideoCallService : Service() {

    private var isPlaying = false
    private lateinit var ringtone: Ringtone
    private val binder = FakeCallBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var overlayView: View? = null
    private lateinit var wakeLock: PowerManager.WakeLock
    private var videoUriString: String? = null

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    inner class FakeCallBinder : Binder() {
        fun getService(): FakeScheduledVideoCallService = this@FakeScheduledVideoCallService
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val callerName = intent?.getStringExtra("CALLER_NAME")
        val triggerTime = intent?.getLongExtra("TRIGGER_TIME", 0L) ?: 0L
        val ringtoneId = intent?.getIntExtra("RINGTONE_ID", R.raw.first_ringtone)
        videoUriString = intent?.getStringExtra("SAVED_VIDEO_URI")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission()) {
            showNotificationPermissionDialog()
            return START_STICKY
        }

        val notification = buildNotification()
        startForeground(1, notification)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FakeScheduledVideoCallService::WakeLock")
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)

        if (triggerTime > System.currentTimeMillis()) {
            scheduleAlarm(triggerTime, callerName)
        } else {
            if (ringtoneId != null) {
                startFakeCall(ringtoneId, intent)
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
    fun scheduleAlarm(triggerTime: Long, callerName: String?) {
        val intent = Intent(this, FakeScheduledCallReceiver::class.java).apply {
            putExtra("CALLER_NAME", callerName)
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun startFakeCall(ringtoneId: Int, intent: Intent) {
        val callerName = intent.getStringExtra("CALLER_NAME")
        if (callerName != null && !isPlaying) {
            isPlaying = true
            playRingtone(ringtoneId)
            showIncomingCallActivity(callerName, ringtoneId, videoUriString ?: "")
            if (Settings.canDrawOverlays(this)) {
                showOverlay(callerName)
            }
        }
    }

    private fun showIncomingCallActivity(callerName: String, ringtoneId: Int, videoUri: String) {
        val intent = Intent(this, IncomingVideoCallActivity::class.java).apply {
            putExtra("CALLER_NAME", callerName)
            putExtra("IS_FROM_SERVICE", true)
            putExtra("RINGTONE_ID", ringtoneId)
            putExtra("SAVED_VIDEO_URI", videoUri)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
        startActivity(intent)
    }

    @SuppressLint("InflateParams")
    private fun showOverlay(callerName: String) {
        if (!Settings.canDrawOverlays(this)) return

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.activity_incoming_video_call, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )
        windowManager.addView(overlayView, params)

        val tvCallerName = overlayView?.findViewById<TextView>(R.id.name_of_the_character_tv_incoming_video_call)
        val btnAccept = overlayView?.findViewById<ImageView>(R.id.accept_icon_incoming_video_call)
        val btnDecline = overlayView?.findViewById<ImageView>(R.id.decline_icon_incoming_video_call)

        tvCallerName?.text = callerName

        btnAccept?.setOnClickListener {
            Toast.makeText(this, "Call Accepted", Toast.LENGTH_SHORT).show()
            stopCall()
            val videoUri = Uri.parse(videoUriString ?: "")
            val intent = Intent(this, AcceptVideoCallActivity::class.java).apply {
                putExtra("SAVED_VIDEO_URI", videoUriString ?: "")
                putExtra("IS_FROM_SERVICE", true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            grantUriPermission("com.example.prankcall", videoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                contentResolver.takePersistableUriPermission(videoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                Log.e("FakeScheduledVideoCallService", "Persistable Uri permission not available", e)
            }
            startActivity(intent)
        }

        btnDecline?.setOnClickListener {
            Toast.makeText(this, "Call Declined", Toast.LENGTH_SHORT).show()
            stopCall()
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }
    }

    private fun buildNotification(): Notification {
        val channelId = "fake_call_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Incoming Fake Video Call")
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

    @RequiresApi(Build.VERSION_CODES.P)
    private fun playRingtone(resId: Int) {
        if (::ringtone.isInitialized && ringtone.isPlaying) {
            ringtone.stop()
        }

        ringtone = RingtoneManager.getRingtone(this, Uri.parse("android.resource://${packageName}/$resId"))
        ringtone.isLooping = true
        ringtone.play()
    }


    override fun onDestroy() {
        super.onDestroy()
        stopCall()
    }

    private fun stopCall() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null

        if (::ringtone.isInitialized) {
            if (ringtone.isPlaying) {
                ringtone.stop()
            }
            // Make sure to release the ringtone if necessary
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

}
