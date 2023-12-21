package com.prestosa.measuremasterchef.spoonacular

data class ConversionResponse(
    val sourceAmount: Double,
    val sourceUnit: String,
    val targetAmount: Double,
    val targetUnit: String,
    val answer: String
)
