                    // RecipeDao.kt
                    package com.prestosa.measuremasterchef.room

                    import androidx.room.Dao
                    import androidx.room.Insert
                    import androidx.room.Query
                    import androidx.room.Update

                    @Dao
                    interface RecipeDao {
                        @Query("SELECT * FROM recipes")
                        suspend fun getAllRecipes(): List<Recipe>

                        @Insert
                        suspend fun insert(recipe: Recipe)

                        @Update
                        suspend fun update(recipe: Recipe)

                        @Query("SELECT * FROM recipes WHERE recipe_name = :recipeName LIMIT 1")
                        suspend fun getRecipeByName(recipeName: String): Recipe?

                        @Query("DELETE FROM recipes WHERE id = :recipeId")
                        suspend fun deleteRecipeById(recipeId: Int)

                        @Query("SELECT * FROM recipes WHERE recipe_name LIKE :query")
                        suspend fun searchRecipes(query: String): List<Recipe>

                    }
