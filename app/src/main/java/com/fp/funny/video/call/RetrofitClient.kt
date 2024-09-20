package com.fp.funny.video.call

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://prankcall.fptechnologyservices.com/")  // Replace with your base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

     val prankVideoItems: PrankVideoItems by lazy {
        retrofit.create(PrankVideoItems::class.java)
    }

}
