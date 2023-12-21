package com.prestosa.measuremasterchef.spoonacular


import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SpoonacularService {
    @GET("/recipes/convert")
    fun convertIngredient(
        @Query("ingredientName") ingredientName: String,
        @Query("sourceAmount") sourceAmount: Double,
        @Query("sourceUnit") sourceUnit: String,
        @Query("targetUnit") targetUnit: String,
        @Query("apiKey") apiKey: String
    ): Call<ConversionResponse>
}
