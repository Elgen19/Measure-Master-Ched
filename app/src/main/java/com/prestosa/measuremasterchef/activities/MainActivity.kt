// MainActivity.kt
package com.prestosa.measuremasterchef.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.prestosa.measuremasterchef.adapters.RecipeAdapter
import com.prestosa.measuremasterchef.databaseoperations.RecipeRepository
import com.prestosa.measuremasterchef.databinding.ActivityMainBinding
import com.prestosa.measuremasterchef.databinding.AddARecipeBottomDialogBinding
import com.prestosa.measuremasterchef.room.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.prestosa.measuremasterchef.room.Recipe
import kotlinx.coroutines.DelicateCoroutinesApi

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = AppDatabase.getInstance(this)
        recipeRepository = RecipeRepository(db.recipeDao(), db.ingredientDao())

        // Initialize the RecipeAdapter
        recipeAdapter = RecipeAdapter(
            emptyList(),
            onItemClick = { clickedRecipe ->
                // Handle item click
                val intent = Intent(this@MainActivity, IngredientListActivity::class.java)
                intent.putExtra("recipe_name", clickedRecipe.recipeName)
                intent.putExtra("serving_size", clickedRecipe.servingSize)
                startActivity(intent)
            },
            onDeleteClick = { deletedRecipe ->
                // Handle delete action, e.g., show confirmation dialog
                showDeleteConfirmationDialog(deletedRecipe)
            }
        )

        binding.fabCreateTweet.setOnClickListener {
            showBottomSheetDialog()
        }

        // Add a TextWatcher to the EditText for search
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for this example
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filter your data based on the search query here
                filterRecipes(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed for this example
            }
        })

        setupRecyclerView()
        refreshRecyclerView()
    }

    private fun filterRecipes(query: String) {
        lifecycleScope.launch {
            val filteredRecipes = recipeRepository.searchRecipes(query)
            recipeAdapter.updateData(filteredRecipes)
        }
    }


    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetBinding = AddARecipeBottomDialogBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.closeButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetBinding.confirmButton.setOnClickListener {
            val recipeName = bottomSheetBinding.recipeName.text.toString()
            val servingSize = bottomSheetBinding.servingSize.text.toString().toIntOrNull()

            if (recipeName.isNotEmpty() && servingSize != null) {
                // Insert into Room database using the repository
                insertRecipeIntoDatabase(recipeName, servingSize)

                // Dismiss the bottom sheet dialog
                bottomSheetDialog.dismiss()
            } else {
                // Handle validation or show a message to the user
            }
        }

        bottomSheetDialog.show()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun insertRecipeIntoDatabase(recipeName: String, servingSize: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            recipeRepository.insertRecipe(recipeName, servingSize)
            refreshRecyclerView()
        }
    }

    private fun setupRecyclerView() {
        // Use lifecycleScope to launch a coroutine
        lifecycleScope.launch {
            val recipes = recipeRepository.getAllRecipes()

            // Update the data in the RecipeAdapter
            recipeAdapter.updateData(recipes)

            binding.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
            binding.recyclerView.adapter = recipeAdapter


        }
    }

    private fun refreshRecyclerView() {
        lifecycleScope.launch {
            val recipes = recipeRepository.getAllRecipes()

            // Update the data in the RecipeAdapter
            recipeAdapter.updateData(recipes)
            // Check if the list of recipes is empty and update the visibility of loadingTextView
            binding.loadingTextView.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showDeleteConfirmationDialog(recipe: Recipe) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Delete Recipe")
        alertDialogBuilder.setMessage("Are you sure you want to delete this recipe?")
        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            deleteRecipeFromDatabase(recipe)
        }
        alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialogBuilder.create().show()
    }

    private fun deleteRecipeFromDatabase(recipe: Recipe) {
        // Implement the deletion of the recipe from the database using the repository
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                recipeRepository.deleteRecipeAndIngredients(recipe.id)
            }
            refreshRecyclerView()
        }
    }
}
