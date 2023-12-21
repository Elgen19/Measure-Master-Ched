package com.prestosa.measuremasterchef.databaseoperations

// RecipeRepository.kt

import android.util.Log
import com.prestosa.measuremasterchef.room.IngredientDao
import com.prestosa.measuremasterchef.room.Recipe
import com.prestosa.measuremasterchef.room.RecipeDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeRepository(private val recipeDao: RecipeDao, private val ingredientDao: IngredientDao) {

    suspend fun insertRecipe(recipeName: String, servingSize: Int) {
        withContext(Dispatchers.IO) {
            try {
                recipeDao.insert(Recipe(0, recipeName, servingSize))
                Log.d("RecipeRepository", "Recipe insertion successful")
            } catch (e: Exception) {
                Log.e("RecipeRepository", "Recipe insertion failed", e)
            }
        }
    }

    suspend fun getAllRecipes(): List<Recipe> {
        return withContext(Dispatchers.IO) {
            recipeDao.getAllRecipes()
        }
    }


    suspend fun getRecipeByName(recipeName: String): Recipe? {
        return withContext(Dispatchers.IO) {
            recipeDao.getRecipeByName(recipeName)
        }
    }

    suspend fun updateRecipe(recipe: Recipe) {
        withContext(Dispatchers.IO) {
            try {
                recipeDao.update(recipe)
                // Log update success
            } catch (e: Exception) {
                // Log update failure
            }
        }
    }

    suspend fun deleteRecipeAndIngredients(recipeId: Int) {
        withContext(Dispatchers.IO) {
            try {
                recipeDao.deleteRecipeById(recipeId)
                val ingredients = ingredientDao.getIngredientsForRecipe(recipeId)
                for (ingredient in ingredients) {
                    ingredientDao.delete(ingredient.id)
                }
                Log.d("RecipeRepository", "Recipe and associated ingredients deleted successfully")
            } catch (e: Exception) {
                Log.e("RecipeRepository", "Error deleting recipe and ingredients", e)
            }
        }
    }

    suspend fun searchRecipes(query: String): List<Recipe> {
        return withContext(Dispatchers.IO) {
            recipeDao.searchRecipes("%$query%")
        }
    }

}
