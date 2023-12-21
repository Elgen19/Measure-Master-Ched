package com.prestosa.measuremasterchef.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "recipe_name") val recipeName: String,
    @ColumnInfo(name = "serving_size") val servingSize: Int
)
