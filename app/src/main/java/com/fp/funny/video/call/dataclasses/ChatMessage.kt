package com.fp.funny.video.call.dataclasses

data class ChatMessage(
    val content: String,
    val imageUri: String = "",
    val isSender: Boolean,
    val isImage: Boolean = imageUri.isNotEmpty()
)

