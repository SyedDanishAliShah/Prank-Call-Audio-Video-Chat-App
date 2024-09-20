package com.fp.funny.video.call.dataclasses

// Data model for individual items
data class FakeCallItem(
    val id: Int,
    val name: String,
    val fake_call_data: Int,
    val phone: String,
    val image_url: String,
    val video_url: String?,
    val audio_url: String?,
    val is_premium: Boolean,
    val is_ad: Boolean
)

// Data model for each category
data class FakeCallCategory(
    val FakeCallData: FakeCallData,
    val data: List<FakeCallItem>
)

// Data model for FakeCallData
data class FakeCallData(
    val id: Int,
    val name: String
)





