package com.prestosa.measuremasterchef.spoonacular

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SpoonacularClient {
    private const val BASE_URL = "https://api.spoonacular.com"

    val service: SpoonacularService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(SpoonacularService::class.java)
    }
}
