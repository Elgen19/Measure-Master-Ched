package com.prestosa.measuremasterchef.spoonacular

import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getString
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.prestosa.measuremasterchef.R
import com.prestosa.measuremasterchef.databaseoperations.IngredientRepository
import com.prestosa.measuremasterchef.databaseoperations.RecipeRepository
import com.prestosa.measuremasterchef.databinding.ConvertIngredientQuantityBinding
import com.prestosa.measuremasterchef.room.Ingredient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// SpoonacularConverter.kt
class SpoonacularConverter(
    private val context: Context,
    private val lifecycleScope: CoroutineScope,
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository,
    private val recipeName: String,
    private val onDataChangedListener: () -> Unit,
) {

    private var isFromToSwapped = false
    private val bottomSheetDialog: BottomSheetDialog = BottomSheetDialog(context)

    fun showConvertBottomSheet() {
        val bottomSheetBinding = ConvertIngredientQuantityBinding.inflate(LayoutInflater.from(context))
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        setupUI(bottomSheetBinding)
        setupButtonClick(bottomSheetBinding, bottomSheetDialog)


        bottomSheetDialog.show()
    }

    private fun setupUI(bottomSheetBinding: ConvertIngredientQuantityBinding) {
        bottomSheetBinding.imageButtonSwapUnits.setOnClickListener {
            // Get the current entries of the spinners
            val fromEntries = context.resources.getStringArray(R.array.metric_measurements)
            val toEntries = context.resources.getStringArray(R.array.imperial_measurements)

            // Swap the entries based on the current state
            if (isFromToSwapped) {
                setSpinnerAdapter(bottomSheetBinding.spinnerFromUnit, fromEntries)
                setSpinnerAdapter(bottomSheetBinding.spinnerToUnit, toEntries)
            } else {
                setSpinnerAdapter(bottomSheetBinding.spinnerFromUnit, toEntries)
                setSpinnerAdapter(bottomSheetBinding.spinnerToUnit, fromEntries)
            }

            // Toggle the state
            isFromToSwapped = !isFromToSwapped
        }
    }

    private fun setSpinnerAdapter(spinner: Spinner, entries: Array<String>) {
        spinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, entries)
    }

    private fun setupButtonClick(
        bottomSheetBinding: ConvertIngredientQuantityBinding,
        bottomSheetDialog: BottomSheetDialog
    ) {
        bottomSheetBinding.buttonConvert.setOnClickListener {
            // Extracted method for clarity
            handleConvertButtonClick(bottomSheetBinding)

        }

        bottomSheetBinding.closeButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }

    private fun handleConvertButtonClick(bottomSheetBinding: ConvertIngredientQuantityBinding) {
        val apiKey = ApiKeyProvider.getApiKey(context)

        val quantity = bottomSheetBinding.editTextQuantity.text.toString().toDoubleOrNull()
        val ingredientName = bottomSheetBinding.editTextIngredient.text.toString()
        val fromUnit = bottomSheetBinding.spinnerFromUnit.selectedItem.toString()
        val toUnit = bottomSheetBinding.spinnerToUnit.selectedItem.toString()

        if (quantity != null && ingredientName.isNotEmpty() && fromUnit.isNotEmpty() && toUnit.isNotEmpty()) {
            performConversion(apiKey, quantity, ingredientName, fromUnit, toUnit, bottomSheetBinding)
        } else {
            bottomSheetBinding.textViewConvertedResult.text = getString(context, R.string.incomplete_input_message)

        }
    }

    private fun performConversion(
        apiKey: String,
        quantity: Double,
        ingredientName: String,
        fromUnit: String,
        toUnit: String,
        bottomSheetBinding: ConvertIngredientQuantityBinding
    ) {
        val call = SpoonacularClient.service.convertIngredient(ingredientName, quantity, fromUnit, toUnit, apiKey)

        call.enqueue(object : Callback<ConversionResponse> {
            override fun onResponse(
                call: Call<ConversionResponse>,
                response: Response<ConversionResponse>
            ) {
                if (response.isSuccessful) {
                    handleSuccessfulResponse(response.body(), bottomSheetBinding)
                } else {
                    handleUnsuccessfulResponse(bottomSheetBinding)
                }
            }

            override fun onFailure(call: Call<ConversionResponse>, t: Throwable) {
                handleNetworkError(bottomSheetBinding)
            }
        })
    }

    private fun handleSuccessfulResponse(
        responseBody: ConversionResponse?,
        bottomSheetBinding: ConvertIngredientQuantityBinding
    ) {
        if (responseBody?.answer != null) {
            // Pass the required parameters to handleValidAnswer
            handleValidAnswer(
                responseBody.answer,
                bottomSheetBinding,
                /* Add the necessary parameters here */
                ingredientName = bottomSheetBinding.editTextIngredient.text.toString(),
                unit = bottomSheetBinding.spinnerToUnit.selectedItem.toString()
            )
        } else {
            bottomSheetBinding.textViewConvertedResult.text = getString(context, R.string.invalid_response)
        }
    }

    private fun handleValidAnswer(
        answer: String,
        bottomSheetBinding: ConvertIngredientQuantityBinding,
        ingredientName: String,
        unit: String
    ) {
        val words = answer.split(" ")

        if (words.size >= 2) {
            val result: String?
            val convertedAmount: Double?

            if (unit != context.getString(R.string.fluid_ounce)) {
                result = "${words[words.size - 2]} ${words[words.size - 1]}"
                bottomSheetBinding.textViewConvertedResult.text = result
                convertedAmount = words[words.size - 2].toDouble()
            }

            else{
                result = "${words[words.size - 3]} ${words[words.size - 2]} ${words[words.size - 1]}"
                bottomSheetBinding.textViewConvertedResult.text = result
                convertedAmount = words[words.size - 3].toDouble()
            }


            // Check if the switch is toggled on
            val switchConvertOrAddIngredient = bottomSheetBinding.switchConvertOrAddIngredient
            if (switchConvertOrAddIngredient.isChecked) {
                // Add the ingredient to the model
                lifecycleScope.launch {
                    addIngredientToModel(convertedAmount, ingredientName, unit)
                    // Delay the execution of onDataChangedListener by a short duration
                    kotlinx.coroutines.delay(200) // You can adjust the duration as needed
                    onDataChangedListener()
                    informationDialog()
                }
            }
        }
    }

    private suspend fun addIngredientToModel(quantity: Double, ingredientName: String, unit: String) {
        lifecycleScope.launch {
            val recipe = recipeName.let { recipeRepository.getRecipeByName(it) }

            if (recipe != null) {
                // Create the Ingredient object with the correct recipeId
                val ingredient = Ingredient(0, ingredientName, quantity, unit, recipe.id)

                // Insert the ingredient into the database using the IngredientRepository
                ingredientRepository.insertIngredient(ingredient)
            }
        }
    }


    private fun handleUnsuccessfulResponse(bottomSheetBinding: ConvertIngredientQuantityBinding) {
        bottomSheetBinding.textViewConvertedResult.text = getString(context, R.string.conversion_failed)
    }

    private fun handleNetworkError(bottomSheetBinding: ConvertIngredientQuantityBinding) {
        bottomSheetBinding.textViewConvertedResult.text = getString(context, R.string.network_error)
    }


    private fun informationDialog() {
        AlertDialog.Builder(context)
            .setTitle("Convert and Add Ingredient")
            .setMessage("Ingredient successfully converted and added to the list.")
            .setPositiveButton("Dismiss", null)
            .show()
    }
}
