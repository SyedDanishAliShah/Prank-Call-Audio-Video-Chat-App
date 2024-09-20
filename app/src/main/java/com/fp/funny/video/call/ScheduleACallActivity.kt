package com.fp.funny.video.call

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.fp.funny.video.call.Ads.AdManager
import com.fp.funny.video.call.broadcastreceiver.FakeScheduledCallReceiver
import com.fp.funny.video.call.databaseclass.AppDatabase
import com.fp.funny.video.call.serviceclass.FakeScheduledCallService
import com.fp.funny.video.call.serviceclass.FakeScheduledCallService.Companion.REQUEST_OVERLAY_PERMISSION
import com.fp.funny.video.call.serviceclass.FakeScheduledVideoCallService
import com.example.call.RemoteConfig.RemoteConfig.isReturningFromExternalActivity
import com.example.call.ads.InterstitialAd.wasInterstitialAdShown
import yuku.ambilwarna.AmbilWarnaDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class ScheduleACallActivity : AppCompatActivity() {

    private lateinit var backIcon: ImageView
    private lateinit var videoCallTv: TextView
    private lateinit var voiceCallTv: TextView
    private lateinit var etEnterName: EditText
    private lateinit var datePicker: DatePicker
    private lateinit var timePicker: TimePicker
    private lateinit var downArrowDatePicker: ImageView
    private lateinit var downArrowTimePicker: ImageView
    private lateinit var selectedDateTextView: TextView
    private lateinit var selectedTimeTextView: TextView
    private lateinit var dropDownIconSelectRingtone: ImageView
    private lateinit var scheduleCallRectangle: ImageView
    private lateinit var database: AppDatabase
    private var fakeCallService: FakeScheduledCallService? = null
    private var isBound = false
    private var videoCallService: FakeScheduledVideoCallService? = null
    private var isVideoCallBound = false
    private var isCallTypeSelected = false // Flag to track if call type is selected
    private var callType = CallType.NONE // Variable to track selected call type
    private lateinit var spinner: Spinner
    private var selectedRingtoneId: Int = R.raw.first_ringtone
    private lateinit var historyRectangle: ImageView
    private lateinit var selectVideoFromGallery: ImageView
    private var selectedVideoUri: String? = null
    private var isVideoSelected = true // Track if video is selected
    private var areEditTextsFilled = true // Track if edit texts are filled
    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 100
    private val READ_MEDIA_VIDEO_REQUEST_CODE = 101
    private lateinit var dropDownIconSelectTheme : ImageView
    private lateinit var adManager: AdManager
    private var selectedYear = -1
    private var selectedMonth = -1
    private var selectedDayOfMonth = -1
    private var selectedHourOfDay = -1
    private var selectedMinute = -1


    private enum class CallType {
        NONE, VOICE, VIDEO
    }


    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = service as FakeScheduledCallService.FakeCallBinder
            fakeCallService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            fakeCallService = null
            isBound = false
        }
    }

    private val videoCallConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = service as FakeScheduledVideoCallService.FakeCallBinder
            videoCallService = binder.getService()
            isVideoCallBound = true
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            videoCallService = null
            isVideoCallBound = false
        }
    }

    private val selectVideoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            selectedVideoUri = data?.data.toString()
            isVideoSelected = true
            checkFieldsAndVideoSelection()
            isReturningFromExternalActivity = true
            Toast.makeText(this, "Video has been selected by user", Toast.LENGTH_SHORT).show()
            }
        }


    @SuppressLint("DefaultLocale", "ClickableViewAccessibility", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_a_call)


        // Initialize AdManager
        adManager = AdManager

        // Load the interstitial ad
        adManager.loadInterstitialAd(this)

                // Check if the permission is already granted
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Device is running Android 13 (API level 33) or higher
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                        != PackageManager.PERMISSION_GRANTED) {

                        // Request the permission for Android 13 or higher
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_MEDIA_VIDEO),
                            READ_MEDIA_VIDEO_REQUEST_CODE
                        )
                    } else {
                        // Permission is already granted, start the service
                        startMyService()
                    }
                } else {
                    // Device is running a version lower than Android 13
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                        // Request the permission for older versions
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            READ_EXTERNAL_STORAGE_REQUEST_CODE
                        )
                    } else {
                        // Permission is already granted, start the service
                        startMyService()
                    }
                }


        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app-database")
            .build()

        backIcon = findViewById(R.id.back_icon_arrow_schedule_a_call)
        voiceCallTv = findViewById(R.id.voice_call_tv)
        videoCallTv = findViewById(R.id.video_call_tv)
        etEnterName = findViewById(R.id.enter_a_name_rectangle_schedule_a_call_et)
        timePicker = findViewById(R.id.time_picker_schedule_a_call)
        datePicker = findViewById(R.id.date_picker_schedule_a_call)
        downArrowTimePicker = findViewById(R.id.downwards_arrow_time_picker)
        downArrowDatePicker = findViewById(R.id.downwards_arrow_date_picker)
        selectedDateTextView = findViewById(R.id.tv_date_picker)
        selectedTimeTextView = findViewById(R.id.tv_time_picker)
        dropDownIconSelectRingtone = findViewById(R.id.downwards_arrow)
        scheduleCallRectangle = findViewById(R.id.schedule_call_rectangle)
        historyRectangle = findViewById(R.id.rectangle_for_history_scheduling_a_call)
        selectVideoFromGallery = findViewById(R.id.gallery_icon_of_scheduling_a_card)
        dropDownIconSelectTheme = findViewById(R.id.downwards_arrow_1)

        // Restore the selected ringtone if it exists
        restoreSelectedRingtone()

        requestIgnoreBatteryOptimizations()

        // Request to disable battery optimizations
        checkBatteryOptimizations()

        updateDateTime()


        selectVideoFromGallery.setOnClickListener {
            if (isVideoSelected && areEditTextsFilled) {
            if (callType == CallType.VIDEO) {
                isReturningFromExternalActivity = true
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                selectVideoLauncher.launch(intent)
            }
            }
            else {
                Toast.makeText(this, "Please select video TV first.", Toast.LENGTH_SHORT).show()
            }
        }


        scheduleCallRectangle.setOnClickListener {
            wasInterstitialAdShown = true
            if (isVideoSelected && areEditTextsFilled) {
                requestOverlayPermissionIfNeeded()
                val callerName = etEnterName.text.toString()
                /*val callerNumber = etEnterNumber.text.toString()*/
                val triggerTime = getTimeInMillis()

                // Check if the trigger time is valid
                if (triggerTime == -1L) {
                    Toast.makeText(
                        this,
                        "The selected date or time is in the past. Cannot schedule the prank call.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                // Show the interstitial ad before scheduling the service
                adManager.showInterstitialAd(this)
                // Schedule the service only if the trigger time is valid
                scheduleService(triggerTime)


            }
            else if (!isVideoSelected) {
                Toast.makeText(this, "Please select a video before scheduling.", Toast.LENGTH_SHORT).show()
            } else if (!areEditTextsFilled) {
                Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show()
            }

            }



        dropDownIconSelectTheme.setOnClickListener {
            val initialColor = 0xFF0000 // Example initial color (red)
            val context = this // or your fragment/activity context

            val colorPicker = AmbilWarnaDialog(context, initialColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {

                    // Store the selected color in SharedPreferences
                    val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putInt("theme_color", color)
                        apply()
                    }

                    // Show a toast message to indicate that the theme color has been selected
                    Toast.makeText(context, "Theme color selected", Toast.LENGTH_SHORT).show()
                }

                override fun onCancel(dialog: AmbilWarnaDialog?) {
                }
            })
// To show the color picker dialog
            colorPicker.show()
        }


        // Listeners for text field changes to check if they are filled
        etEnterName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkEditTextFields()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        historyRectangle.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
            finish()
        }

        createNotificationChannel()


        // Hide the DatePicker and TimePicker initially
        datePicker.visibility = View.INVISIBLE
        timePicker.visibility = View.INVISIBLE

        backIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        voiceCallTv.setOnClickListener {
            setSelectedTextView(voiceCallTv, videoCallTv)
            callType = CallType.VOICE
            isReturningFromExternalActivity = true
            isCallTypeSelected = true
        }

        videoCallTv.setOnClickListener {
            setSelectedTextView(videoCallTv, voiceCallTv)
            callType = CallType.VIDEO
            isCallTypeSelected = true
            selectVideoFromGallery.isEnabled = true

        }

        // Intercept touch events for EditTexts
        dropDownIconSelectTheme.setOnTouchListener { _, event ->
            if (!isCallTypeSelected) {
                if (event.action == MotionEvent.ACTION_UP) {
                    Toast.makeText(this, "Kindly select video or voice call option first", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        etEnterName.setOnTouchListener { _, event ->
            if (!isCallTypeSelected) {
                if (event.action == MotionEvent.ACTION_UP) {
                    Toast.makeText(this, "Kindly select video or voice call option first", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        // Show DatePicker when downArrowDatePicker is clicked
        downArrowDatePicker.setOnClickListener {
            if (isCallTypeSelected) {
                val calendar = Calendar.getInstance()
                val datePickerDialog = DatePickerDialog(
                    this,
                    { _, year, month, dayOfMonth ->
                        // Store the selected date in the global variables
                        selectedYear = year
                        selectedMonth = month
                        selectedDayOfMonth = dayOfMonth

                        // Update TextView with selected date
                        val selectedDate = "$dayOfMonth/${month + 1}/$year"
                        selectedDateTextView.text = selectedDate
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                datePickerDialog.show()
            } else {
                Toast.makeText(this, "Kindly select video or voice call option first", Toast.LENGTH_SHORT).show()
            }
        }

        // Show TimePicker when downArrowTimePicker is clicked
        downArrowTimePicker.setOnClickListener {
            if (isCallTypeSelected) {
                val calendar = Calendar.getInstance()
                val timePickerDialog = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        // Store the selected time in the global variables
                        selectedHourOfDay = hourOfDay
                        selectedMinute = minute

                        // Update TextView with selected time
                        val selectedTime = convertTo12HourFormat(hourOfDay, minute)
                        selectedTimeTextView.text = selectedTime
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                )
                timePickerDialog.show()
            } else {
                Toast.makeText(this, "Kindly select video or voice call option first", Toast.LENGTH_SHORT).show()
            }
        }


        // Handle DatePicker date selection
        datePicker.setOnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
            // Hide DatePicker and update TextView with selected date
            datePicker.visibility = View.INVISIBLE
            val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
            selectedDateTextView.text = selectedDate
        }

        // Handle TimePicker time selection
        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            // Hide TimePicker and update TextView with selected time
            timePicker.visibility = View.INVISIBLE
            val selectedTime = convertTo12HourFormat(hourOfDay, minute)
            selectedTimeTextView.text = selectedTime
        }


        // Define your spinner items and their corresponding sound resource IDs
        val soundNames = arrayOf("Ringtone 1", "Ringtone 2", "Ringtone 3", "Ringtone 4", "Ringtone 5", "Ringtone 6", "Ringtone 7")
        val soundResourceIds = arrayOf(R.raw.first_ringtone, R.raw.second_ringtone, R.raw.third_ringtone, R.raw.fourth_ringtone, R.raw.fifth_ringtone, R.raw.sixth_ringtone, R.raw.seventh_ringtone)

// Populate the spinner
        spinner = findViewById(R.id.ringtone_spinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, soundNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.setOnTouchListener { _, event ->
            if (!isCallTypeSelected) {
                if (event.action == MotionEvent.ACTION_UP) {
                    Toast.makeText(this, "Kindly select video or voice call option first", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        dropDownIconSelectRingtone.setOnClickListener {
            if (isCallTypeSelected) {
                spinner.performClick()
            } else {
                Toast.makeText(this, "Kindly select video or voice call option first", Toast.LENGTH_SHORT).show()
            }
        }

        // Set an OnItemSelectedListener on the Spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // Get the selected sound resource ID
                selectedRingtoneId = soundResourceIds[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
        scheduleCallRectangle.isEnabled = false
    }

    private fun checkBatteryOptimizations() {
        val packageName = packageName
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            Log.d("BatteryOptimization", "Battery optimization is enabled for the app.")
        } else {
            Log.d(
                "BatteryOptimization",
                "Battery optimization is already disabled for the app."
            )
        }
    }

    @SuppressLint("BatteryLife")
    private fun requestIgnoreBatteryOptimizations() {
        val packageName = packageName
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            Log.d("BatteryOptimization", "Requesting to disable battery optimizations.")
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            if (intent.resolveActivity(packageManager) != null) {
                Log.d(
                    "BatteryOptimization",
                    "Intent to request ignoring battery optimizations is available."
                )
                startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATIONS)
            } else {
                Log.d(
                    "BatteryOptimization",
                    "Intent to request ignoring battery optimizations is NOT available."
                )
            }
        } else {
            Log.d("BatteryOptimization", "Battery optimization is already ignored for this app.")
        }
    }

    companion object {
        private const val REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1001
    }

    private fun checkEditTextFields() {
        areEditTextsFilled = etEnterName.text.isNotEmpty()
        checkFieldsAndVideoSelection()
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                onOverlayPermissionGranted()
            } else {
                Toast.makeText(this, "Overlay permission not granted. Cannot schedule the call.", Toast.LENGTH_SHORT).show()
            }
        }

        if (requestCode == REQUEST_CODE_BATTERY_OPTIMIZATIONS) {
            val packageName = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
                Log.d("BatteryOptimization", "Battery optimization disabled successfully.")
                Toast.makeText(this, "Battery optimization disabled", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("BatteryOptimization", "Battery optimization not disabled.")
                Toast.makeText(this, "Battery optimization not disabled", Toast.LENGTH_SHORT).show()
            }
        }


        // Handle notification permission dialog
        if (intent.getBooleanExtra("SHOW_PERMISSION_DIALOG", false)) {
            showNotificationPermissionDialog()
        }
        isReturningFromExternalActivity = true
    }

    private fun showNotificationPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Notification Permission Needed")
            .setMessage("This app requires notification permission to display fake call notifications. Please enable notifications for this app.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun isValidTime(triggerTime: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        return triggerTime > currentTime
    }

    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Call Channel"
            val descriptionText = "Channel for Call notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("fake_call_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getTimeInMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth,
            timePicker.hour, timePicker.minute, 0)
        return calendar.timeInMillis
    }

    private fun setSelectedTextView(selectedTextView: TextView, otherTextView: TextView) {
        // Set padding to the selected TextView to make the background drawable appear larger
        selectedTextView.setPadding(23, 10, 23, 10) // Adjust padding as needed

        // Apply the drawable background
        selectedTextView.setBackgroundResource(R.drawable.rounded_corners_for_selection_of_video_voice)

        // Apply a default background or transparent color to the other TextView
        otherTextView.setBackgroundColor(Color.TRANSPARENT)
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleService(triggerTime: Long) {
        if (!isValidTime(triggerTime)) {
            // Do not trigger the service, optionally show a message to the user
            //Toast.makeText(this, "Selected date is in the past. Please choose a future date.", Toast.LENGTH_LONG).show()
            return
        }

        if (!hasOverlayPermission()) {
            Toast.makeText(this, "Overlay permission is not granted", Toast.LENGTH_SHORT).show()
            requestOverlayPermissionIfNeeded()
            return
        }

        // Proceed with scheduling the service as the permission is granted
        scheduleAlarm(triggerTime)
    }
    private fun scheduleAlarm(triggerTime: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, FakeScheduledCallReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, alarmIntent)

        // Optionally show a message to the user indicating the alarm is set
        Toast.makeText(this, "Service scheduled successfully.", Toast.LENGTH_LONG).show()
    }

    private fun scheduleFakeCall(callerName: String, triggerTime: Long) {
        val intent = if (callType == CallType.VOICE) {
            Intent(this, FakeScheduledCallService::class.java)
        } else {
            Intent(this, FakeScheduledVideoCallService::class.java)
        }
        intent.putExtra("CALLER_NAME", callerName)
        intent.putExtra("TRIGGER_TIME", triggerTime)
        intent.putExtra("CALL_TYPE", callType.name)
        intent.putExtra("RINGTONE_ID", selectedRingtoneId)
        intent.putExtra("SAVED_VIDEO_URI", selectedVideoUri)



        val pendingIntent = PendingIntent.getService(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)

       // startMainActivity()

        Toast.makeText(this, "Scheduled a ${callType.name.lowercase(Locale.ROOT)} call for $callerName at ${selectedDateTextView.text} ${selectedTimeTextView.text}", Toast.LENGTH_SHORT).show()
        Log.d("ScheduleACallActivity", "Scheduled a ${callType.name.lowercase(Locale.ROOT)} call for $callerName at ${selectedDateTextView.text} ${selectedTimeTextView.text}")
    }


    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun convertTo12HourFormat(hourOfDay: Int, minute: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
        cal.set(Calendar.MINUTE, minute)
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(cal.time)
    }


    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, FakeScheduledCallService::class.java)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        // Bind to FakeScheduledVideoCallService
        val videoCallServiceIntent = Intent(this, FakeScheduledVideoCallService::class.java)
        bindService(videoCallServiceIntent, videoCallConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }

        // Unbind from FakeScheduledVideoCallService
        if (isVideoCallBound) {
            unbindService(videoCallConnection)
            isVideoCallBound = false
        }
    }

    private fun saveSelectedRingtone() {
        val selectedRingtone = spinner.selectedItem.toString()
        val sharedPreferences = getSharedPreferences("RingtonePrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("selected_ringtone", selectedRingtone)
            apply()
        }
    }

    private fun restoreSelectedRingtone() {
        spinner = findViewById(R.id.ringtone_spinner)
        val sharedPreferences = getSharedPreferences("RingtonePrefs", MODE_PRIVATE)
        val selectedRingtone = sharedPreferences.getString("selected_ringtone", "Ringtone 1") ?: "Ringtone 1"

        @Suppress("UNCHECKED_CAST")
        val adapter = spinner.adapter as? ArrayAdapter<String>
        adapter?.let {
            val spinnerPosition = it.getPosition(selectedRingtone)
            if (spinnerPosition >= 0) {
                spinner.setSelection(spinnerPosition)
            }
        }
    }

    private fun requestOverlayPermissionIfNeeded() {
        if (!Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this)
                .setTitle("Permission required")
                .setMessage("This functionality requires 'Display over other apps' permission. Please grant it to proceed.")
                .setPositiveButton("OK") { _, _ ->
                    saveSelectedRingtone()
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            // Permission already granted, proceed with the action
            onOverlayPermissionGranted()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted, start the service
                    startMyService()
                } else {
                    // Permission denied
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        restoreSelectedRingtone()
        updateDateTime()
    }

    private fun startMyService() {
        val intent = Intent(this, FakeScheduledVideoCallService::class.java)
        startService(intent)
    }


    // Function to check if all conditions are met to enable the button
    private fun checkFieldsAndVideoSelection() {
        scheduleCallRectangle.isEnabled = isCallTypeSelected && areEditTextsFilled && (callType == CallType.VOICE || (callType == CallType.VIDEO && isVideoSelected))
    }



    private fun updateDateTime() {
        val currentDateTime = System.currentTimeMillis()

        val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val dateString = dateFormatter.format(currentDateTime)
        selectedDateTextView.text = dateString

        // Time format: HH:MM AM/PM
        val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val timeString = timeFormatter.format(currentDateTime)
        selectedTimeTextView.text = timeString
    }

    private fun proceedToScheduleCall(triggerTime: Long) {
        val callerName = etEnterName.text.toString()
        adManager.showInterstitialAd(this)
        scheduleFakeCall(callerName, triggerTime)
    }

    private fun onOverlayPermissionGranted() {
        // Check if date and time have been selected
        if (selectedYear != -1 && selectedMonth != -1 && selectedDayOfMonth != -1 && selectedHourOfDay != -1 && selectedMinute != -1) {
            // Create a Calendar object
            val calendar = Calendar.getInstance()

            // Set the selected date and time in the Calendar object
            calendar.set(selectedYear, selectedMonth, selectedDayOfMonth, selectedHourOfDay, selectedMinute)

            // Calculate the trigger time
            val triggerTimeInMillis = calendar.timeInMillis

            // Get the current time
            val currentTimeInMillis = System.currentTimeMillis()

            if (triggerTimeInMillis > currentTimeInMillis) {
                // Proceed with scheduling
                proceedToScheduleCall(triggerTimeInMillis)
            } else {
                // Selected time is in the past
                Toast.makeText(this, "Selected time or date is in the past. Please select a future time or date.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Date or time not selected
            Toast.makeText(this, "Please select both a date and a time.", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }

        // Unbind from FakeScheduledVideoCallService
        if (isVideoCallBound) {
            unbindService(videoCallConnection)
            isVideoCallBound = false
        }
    }

}


