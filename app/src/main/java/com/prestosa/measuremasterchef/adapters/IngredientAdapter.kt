// IngredientAdapter.kt
package com.prestosa.measuremasterchef.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.prestosa.measuremasterchef.R
import com.prestosa.measuremasterchef.databinding.IngredientItemBinding
import com.prestosa.measuremasterchef.room.Ingredient
import com.prestosa.measuremasterchef.room.Recipe

class IngredientAdapter(
    private var ingredients: List<Ingredient>,
    private val onDeleteClick: (Ingredient) -> Unit) :
    RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    class IngredientViewHolder(private val binding: IngredientItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(ingredient: Ingredient, onDeleteClick: (Ingredient) -> Unit) {
            binding.ingredientNameTextView.text = ingredient.ingredientName
            binding.quantityUnitTextView.text = "${ingredient.quantity} ${ingredient.unit}"

            binding.imageButton.setOnClickListener { view ->
                showPopupMenu(view, ingredient, onDeleteClick)
            }
        }

        private fun showPopupMenu(view: View, ingredient: Ingredient, onDeleteClick: (Ingredient) -> Unit) {
            val popupMenu = PopupMenu(view.context, view)
            val inflater: MenuInflater = popupMenu.menuInflater
            inflater.inflate(R.menu.delete_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_delete -> {
                        onDeleteClick(ingredient)
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding =
            IngredientItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val currentIngredient = ingredients[position]
        holder.bind(currentIngredient, onDeleteClick)
    }

    override fun getItemCount(): Int {
        return ingredients.size
    }


    fun updateDataWithoutRefresh(newData: List<Ingredient>) {
        ingredients = newData.toMutableList()
        notifyDataSetChanged()
    }






}
