package com.fp.funny.video.call

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fp.funny.video.call.adapters.ChatMessageAdapter
import com.fp.funny.video.call.dataclasses.ChatMessage
import com.example.call.RemoteConfig.RemoteConfig.isReturningFromExternalActivity

class FakeChatActivity : AppCompatActivity() {

    private lateinit var etTypeAMessage: EditText
    lateinit var sendMessageIcon: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: ChatMessageAdapter
    private val messageList = mutableListOf<ChatMessage>()
    private lateinit var characterImage: ImageView
    private lateinit var backIcon: ImageView
    private lateinit var voiceCallIcon: ImageView
    private lateinit var nameOfCharacterTv: TextView
    private lateinit var videoCallIcon: ImageView
    private lateinit var snapShotIcon: ImageView
    private var customDialog: AlertDialog? = null
    private lateinit var rootView: View
    private var pendingMessage: String? = null
    private var isSenderSelected: Boolean = true

    companion object {
        private const val REQUEST_CODE_SELECT_IMAGE = 1
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fake_chat)

        rootView = findViewById(R.id.root_view_fake_chat_activity)
        characterImage = findViewById(R.id.imageView_character)
        backIcon = findViewById(R.id.back_icon)
        voiceCallIcon = findViewById(R.id.voice_call_icon)
        nameOfCharacterTv = findViewById(R.id.name_of_character_fake_chat)
        videoCallIcon = findViewById(R.id.video_call_icon)
        snapShotIcon = findViewById(R.id.camera_icon_fake_chat)
        etTypeAMessage = findViewById(R.id.et_type_a_message)
        sendMessageIcon = findViewById(R.id.send_message_icon)
        recyclerView = findViewById(R.id.rv_sender_message)
        val videoUrl = intent.getStringExtra("videoUrl")

        messageAdapter = ChatMessageAdapter(messageList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = messageAdapter

        snapShotIcon.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
            isReturningFromExternalActivity = true

        }

        val imageResId = intent.getStringExtra("imageResId")
        if (imageResId != null) {
            Glide.with(this)
                .load(imageResId)
                .apply(RequestOptions.circleCropTransform())
                .into(characterImage)

        }

        val nameOfCharacter = intent.getStringExtra("nameResId")
        if (nameOfCharacter != null) {
            nameOfCharacterTv.text = nameOfCharacter
        }

        val selectedThemeColor = intent.getIntExtra("theme_color", 0)

        backIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        sendMessageIcon.setOnClickListener {
            val message = etTypeAMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                pendingMessage = message
                etTypeAMessage.text.clear()
                showSenderReceiverDialog(Uri.EMPTY)
            }
        }

        voiceCallIcon.setOnClickListener {
            val intent = Intent(this, IncomingVoiceCallActivity::class.java)
            intent.putExtra("IS_FROM_FAKE_CHAT", true)  // Add this flag
            intent.putExtra("imageResId", imageResId)
                .putExtra("nameResId", nameOfCharacter)
                .putExtra("theme_color", selectedThemeColor)
            startActivity(intent)
        }

        videoCallIcon.setOnClickListener {
            val intent = Intent(this, IncomingVideoCallActivity::class.java)
            intent.putExtra("IS_FROM_FAKE_CHAT", true)  // Add this flag
            intent.putExtra("imageResId", imageResId)
            intent.putExtra("nameResId", nameOfCharacter)
            intent.putExtra("videoUrl",videoUrl)
            startActivity(intent)
        }

        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.height
            val keypadHeight = screenHeight - rect.bottom
            if (keypadHeight > screenHeight * 0.15) {
                // Keyboard is opened
                adjustDialogPosition()
            } else {
                // Keyboard is closed
                adjustDialogPosition()
            }
        }
    }


    private fun adjustDialogPosition() {
        customDialog?.let {
            val sendMessageIconLocation = IntArray(2)
            sendMessageIcon.getLocationOnScreen(sendMessageIconLocation)
            val layoutParams = it.window?.attributes
            layoutParams?.x = sendMessageIconLocation[0] - sendMessageIcon.width / 2
            layoutParams?.y = sendMessageIconLocation[1] - rootView.height / 3
            it.window?.attributes = layoutParams
        }
    }

    private fun showSenderReceiverDialog(imageUri: Uri) {
        val dialog = SenderReceiverDialog(
            context = this,
            onSenderSelected = {
                isSenderSelected = true
                if (imageUri != Uri.EMPTY) {
                    addImageToRecyclerView(pendingMessage ?: "", imageUri, true)
                } else {
                    addTextMessageToRecyclerView(pendingMessage ?: "", true)
                }
            },
            onReceiverSelected = {
                isSenderSelected = false
                if (imageUri != Uri.EMPTY) {
                    addImageToRecyclerView(pendingMessage ?: "", imageUri, false)
                } else {
                    addTextMessageToRecyclerView(pendingMessage ?: "", false)
                }
            },
            messageAdapter
        )

        dialog.setCancelable(true)

        // Set dialog width and height to a percentage of the screen dimensions for consistency
        dialog.setOnShowListener {
            val displayMetrics = resources.displayMetrics
            val width = (displayMetrics.widthPixels * 0.62).toInt()
            val height = (displayMetrics.heightPixels * 0.22).toInt()
            dialog.window?.setLayout(width, height)

            // Position dialog based on screen dimensions
            val layoutParams = dialog.window?.attributes
            layoutParams?.x = (displayMetrics.widthPixels * 0.5 - width / 2).toInt()
            layoutParams?.y = (displayMetrics.heightPixels * 0.75 - height / 2).toInt()
            dialog.window?.attributes = layoutParams
        }

        dialog.show()
    }






    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { imageUri ->
                showSenderReceiverDialog(imageUri)
            }
        }
         isReturningFromExternalActivity = true
    }

    private fun addImageToRecyclerView(message: String, imageUri: Uri, isSender: Boolean) {
        val chatMessage = ChatMessage(message, imageUri.toString(), isSender)
        messageList.add(chatMessage)
        messageAdapter.notifyItemInserted(messageList.size - 1)
        recyclerView.scrollToPosition(messageList.size - 1)
        etTypeAMessage.text.clear()
    }
    private fun addTextMessageToRecyclerView(message: String, isSender: Boolean) {
        val chatMessage = ChatMessage(content = message, isSender = isSender)
        messageList.add(chatMessage)
        messageAdapter.notifyItemInserted(messageList.size - 1)
        recyclerView.scrollToPosition(messageList.size - 1)
        etTypeAMessage.text.clear()
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()

    }

}









