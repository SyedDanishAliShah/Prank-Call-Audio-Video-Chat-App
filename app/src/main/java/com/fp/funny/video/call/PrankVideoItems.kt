package com.fp.funny.video.call

import com.fp.funny.video.call.dataclasses.FakeCallCategory
import retrofit2.Response
import retrofit2.http.GET

interface PrankVideoItems {
    @GET("api/data/")
    suspend fun getPrankVideoData(): Response<List<FakeCallCategory>>
}
