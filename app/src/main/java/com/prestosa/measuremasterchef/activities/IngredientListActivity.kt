// IngredientListActivity.kt
package com.prestosa.measuremasterchef.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.prestosa.measuremasterchef.R
import com.prestosa.measuremasterchef.adapters.IngredientAdapter
import com.prestosa.measuremasterchef.databaseoperations.IngredientRepository
import com.prestosa.measuremasterchef.databaseoperations.RecipeRepository
import com.prestosa.measuremasterchef.databinding.ActivityIngredientListBinding
import com.prestosa.measuremasterchef.databinding.AddARecipeBottomDialogBinding
import com.prestosa.measuremasterchef.databinding.BottomSheetAddIngredientBinding
import com.prestosa.measuremasterchef.room.AppDatabase
import com.prestosa.measuremasterchef.room.Ingredient
import com.prestosa.measuremasterchef.spoonacular.SpoonacularConverter
import kotlinx.coroutines.launch


class IngredientListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIngredientListBinding
    private lateinit var ingredientAdapter: IngredientAdapter
    private lateinit var ingredientRepository: IngredientRepository
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var popupMenu: PopupMenu
    private lateinit var spoonacularConverter: SpoonacularConverter
    private var recipeName: String? = null
    private var originalServingSize: Int = 0
    private var updatedServingSize: Int = 0
    private var isAnUpdateToServingSize: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIngredientListBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize RecyclerView and its adapter
        ingredientAdapter = IngredientAdapter(emptyList()) { ingredient ->
            // Handle delete action, e.g., show confirmation dialog
            showDeleteConfirmationDialog(ingredient)
        }

        binding.ingredientsRecyclerView.adapter = ingredientAdapter
        binding.ingredientsRecyclerView.layoutManager = LinearLayoutManager(this)


        // Initialize ingredient repository
        val db = AppDatabase.getInstance(this)
        ingredientRepository = IngredientRepository(db.ingredientDao())

        // Initialize recipe repository
        recipeRepository = RecipeRepository(db.recipeDao(), db.ingredientDao())

        // Retrieve data from intent
        recipeName = intent.getStringExtra("recipe_name")
        val servingSize = intent.getIntExtra("serving_size", 0)

        // Set data to TextViews
        binding.recipeNameIngredientList.text = recipeName
        binding.servingSizeIngredientList.text = getString(R.string.serving_size_label, servingSize)

        // Initialize PopupMenu
        popupMenu = PopupMenu(this, binding.fabAddIngredient)
        setupPopupMenu()

        binding.fabAddIngredient.setOnClickListener {
            // Show the PopupMenu
            popupMenu.show()
        }

        // Set up the CardView click listener
        binding.cardView.setOnClickListener {
            showEditRecipeBottomSheet()
        }

        // Load ingredients from the database when the activity is created
        loadIngredientsFromDatabase()

        // Initialize SpoonacularConverter
        spoonacularConverter = SpoonacularConverter(
            this,
            lifecycleScope,
            ingredientRepository,
            recipeRepository,
            recipeName ?: "",
            ::loadIngredientsFromDatabase,
        )

    }

    override fun onResume() {
        super.onResume()
        loadIngredientsFromDatabase()
    }

    private fun setupPopupMenu() {
        // Inflate the menu resource
        popupMenu.menuInflater.inflate(R.menu.ingredient_list_popup_menu, popupMenu.menu)

        // Set a listener for menu item clicks
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_convert -> {
                    showConvertBottomSheet()
                    true
                }
                R.id.menu_add -> {
                    // Show the "Add Ingredient" bottom sheet
                    showAddIngredientBottomSheet()
                    true
                }
                else -> false
            }
        }
    }

    private fun showConvertBottomSheet(){
        // Delegate the conversion logic to SpoonacularConverter
        spoonacularConverter.showConvertBottomSheet()
    }

    private fun showEditRecipeBottomSheet() {
        val bottomSheetBinding = AddARecipeBottomDialogBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        // Set up the bottom sheet views and actions

        // rename button confirm text to update. I am reusing the add a recipe layout here.
        bottomSheetBinding.confirmButton.text = getString(R.string.update)

        // Populate existing data
        bottomSheetBinding.recipeName.setText(binding.recipeNameIngredientList.text)
        // Assuming binding.servingSizeIngredientList.text is in the format "Serving Size: 1"
        val servingSizeText = binding.servingSizeIngredientList.text.toString()

        // Extract only the number from the serving size text
        val servingSizeNumber = servingSizeText.replace(Regex("[^0-9]"), "")

        // Extract the original serving size and convert it to Integer.
        originalServingSize = servingSizeNumber.toInt()

        // Set the extracted number in the EditText
        bottomSheetBinding.servingSize.setText(servingSizeNumber)


        // Close button
        bottomSheetBinding.closeButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // Confirm button
        bottomSheetBinding.confirmButton.setOnClickListener {
            // Handle the Confirm button click
            val editedRecipeName = bottomSheetBinding.recipeName.text.toString()
            val editedServingSize = bottomSheetBinding.servingSize.text.toString()

            // Get the edited serving size and convert it to Int
            updatedServingSize = editedServingSize.toInt()



            // Retrieve the corresponding Recipe from the database based on the original recipeName
            lifecycleScope.launch {
                val originalRecipe = recipeName?.let { recipeRepository.getRecipeByName(it) }

                if (originalRecipe != null) {

                    // set the flag to true
                    isAnUpdateToServingSize = true

                    // Update the Recipe object with the edited data
                    val editedRecipe = originalRecipe.copy(
                        recipeName = editedRecipeName,
                        servingSize = editedServingSize.toIntOrNull() ?: 0
                    )

                    // Update the recipe in the database using the RecipeRepository
                    recipeRepository.updateRecipe(editedRecipe)
                    loadIngredientsFromDatabase()


                    // Log the update success
                }

                // Update the activity views with edited data
                binding.recipeNameIngredientList.text = editedRecipeName
                binding.servingSizeIngredientList.text = getString(R.string.serving_size_format, editedServingSize)
            }

            // Dismiss the bottom sheet after editing
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }



    private fun showAddIngredientBottomSheet() {
        val bottomSheetBinding = BottomSheetAddIngredientBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        // Set up the bottom sheet views and actions

        // Close button
        bottomSheetBinding.closeButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // Add Ingredient button
        bottomSheetBinding.addIngredientButton.setOnClickListener {
            // Handle the Add Ingredient button click
            // You can access the ingredient name, quantity, and unit from the corresponding views
            val ingredientName = bottomSheetBinding.ingredientNameEditText.text.toString()
            val quantity = bottomSheetBinding.quantityEditText.text.toString().toDoubleOrNull()
            val unit = bottomSheetBinding.unitSpinner.selectedItem.toString()

            if (quantity != null) {
                // Retrieve the corresponding Recipe from the database based on recipeName
                lifecycleScope.launch {
                    val recipe = recipeName?.let { recipeRepository.getRecipeByName(it) }

                    if (recipe != null) {
                        // Create the Ingredient object with the correct recipeId
                        val ingredient = Ingredient(0, ingredientName, quantity, unit, recipe.id)

                        // Insert the ingredient into the database using the IngredientRepository
                        ingredientRepository.insertIngredient(ingredient)

                        // Load ingredients from the database when the activity is created
                        loadIngredientsFromDatabase()



                        // Dismiss the bottom sheet after adding the ingredient
                        bottomSheetDialog.dismiss()
                    } else {
                        // Handle the case when the recipe is not found
                        // You can show an error message or perform other actions
                    }
                }
            } else {
                // Handle invalid quantity input
                // You can show an error message or perform other actions
            }
        }

        bottomSheetDialog.show()
    }




    private fun loadIngredientsFromDatabase() {
        lifecycleScope.launch {
            val recipe = recipeName?.let { recipeRepository.getRecipeByName(it) }
            val recipeId = recipe?.id

            if (recipeId != null) {
                // Retrieve ingredients for the specified recipe from the database
                val ingredients = ingredientRepository.getIngredientsForRecipe(recipeId)
                val adjustedIngredients: List<Ingredient>


                // Log the ingredients data
                for (ingredient in ingredients) {
                    Log.d("IngredientListActivity", "Ingredient: $ingredient")
                }

                if (isAnUpdateToServingSize) {
                    // Adjust ingredient quantities based on the updated serving size
                    adjustedIngredients = adjustIngredientQuantities(ingredients)

                    // Update the adapter with the new data
                    ingredientAdapter.updateDataWithoutRefresh(adjustedIngredients)

                    // Update ingredient quantities in the database
                    updateIngredientQuantitiesInDatabase(adjustedIngredients)

                    isAnUpdateToServingSize = false
                } else {
                    // Update the adapter with the new data
                    ingredientAdapter.updateDataWithoutRefresh(ingredients)
                }


                binding.noIngredientsTextView.visibility =
                    if (ingredients.isEmpty()) View.VISIBLE else View.GONE


            }
        }
    }

    private fun adjustIngredientQuantities(ingredients: List<Ingredient>): List<Ingredient> {
        // Check for division by zero
        if (originalServingSize == 0 || updatedServingSize == 0) {
            return ingredients
        }

        // Adjust ingredient quantities based on the updated serving size
        return ingredients.map { ingredient ->
            val adjustedQuantity =
                String.format("%.2f", (ingredient.quantity / originalServingSize) * updatedServingSize)
                    .toDouble()
            ingredient.copy(quantity = adjustedQuantity)
        }
    }


    private fun updateIngredientQuantitiesInDatabase(ingredients: List<Ingredient>) {
        lifecycleScope.launch {
            for (ingredient in ingredients) {
                // Update each ingredient in the database
                ingredientRepository.updateIngredient(ingredient)
            }
        }
    }

    private fun showDeleteConfirmationDialog(ingredient: Ingredient) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete))
            .setMessage("Are you sure you want to delete this ingredient?")
            .setPositiveButton("Yes") { _, _ ->
                onDeleteConfirmed(ingredient)
            }
            .setNegativeButton("No", null)
            .show()
    }



    private fun onDeleteConfirmed(ingredient: Ingredient) {
        // Implement the deletion logic here
        // Call the delete method in the repository or ViewModel
        // Update the RecyclerView accordingly
        lifecycleScope.launch {
            ingredientRepository.deleteIngredient(ingredient)
            loadIngredientsFromDatabase()
        }
    }





}
