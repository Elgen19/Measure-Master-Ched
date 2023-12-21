package com.prestosa.measuremasterchef.room

// IngredientDao.kt


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients WHERE recipe_id = :recipeId")
    suspend fun getIngredientsForRecipe(recipeId: Int): List<Ingredient>

    @Insert
    suspend fun insert(ingredient: Ingredient)

    @Update
    suspend fun update(ingredient: Ingredient)

    @Query("DELETE FROM ingredients WHERE id = :ingredientId")
    suspend fun delete(ingredientId: Int)
}
