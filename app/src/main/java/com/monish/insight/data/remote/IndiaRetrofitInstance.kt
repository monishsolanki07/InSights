package com.monish.insight.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object IndiaRetrofitInstance {
    private const val BASE_URL = "https://api.worldnewsapi.com/"

    val api: IndiaNewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IndiaNewsApiService::class.java)
    }
}
