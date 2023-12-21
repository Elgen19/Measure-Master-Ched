// IngredientRepository.kt
package com.prestosa.measuremasterchef.databaseoperations

import android.util.Log
import com.prestosa.measuremasterchef.room.Ingredient
import com.prestosa.measuremasterchef.room.IngredientDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IngredientRepository(private val ingredientDao: IngredientDao) {

    suspend fun insertIngredient(ingredient: Ingredient) {
        withContext(Dispatchers.IO) {
            try {
                ingredientDao.insert(ingredient)
                Log.d("IngredientRepository", "Ingredient insertion successful $ingredient")
            } catch (e: Exception) {
                Log.e("IngredientRepository", "Ingredient insertion failed", e)
            }
        }
    }

    suspend fun updateIngredient(ingredient: Ingredient) {
        withContext(Dispatchers.IO) {
            try {
                ingredientDao.update(ingredient)
                Log.d("IngredientRepository", "Ingredient update successful $ingredient")
            } catch (e: Exception) {
                Log.e("IngredientRepository", "Ingredient update failed", e)
            }
        }
    }

    suspend fun deleteIngredient(ingredient: Ingredient) {
        withContext(Dispatchers.IO) {
            try {
                ingredientDao.delete(ingredient.id)
                Log.d("IngredientRepository", "Ingredient delete successful $ingredient")
            } catch (e: Exception) {
                Log.e("IngredientRepository", "Ingredient delete failed", e)
            }
        }
    }

    suspend fun getIngredientsForRecipe(recipeId: Int): List<Ingredient> {
        return withContext(Dispatchers.IO) {
            ingredientDao.getIngredientsForRecipe(recipeId)
        }
    }
}
