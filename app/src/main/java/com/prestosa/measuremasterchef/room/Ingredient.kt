package com.prestosa.measuremasterchef.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients", foreignKeys = [ForeignKey(entity = Recipe::class, parentColumns = ["id"], childColumns = ["recipe_id"], onDelete = CASCADE)])
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "ingredient_name") val ingredientName: String,
    @ColumnInfo(name = "quantity") val quantity: Double,
    @ColumnInfo(name = "unit") val unit: String,
    @ColumnInfo(name = "recipe_id") val recipeId: Int
)
