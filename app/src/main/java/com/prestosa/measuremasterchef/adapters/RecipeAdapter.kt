package com.prestosa.measuremasterchef.adapters

// RecipeAdapter.kt

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.prestosa.measuremasterchef.R
import com.prestosa.measuremasterchef.databinding.RecipeItemBinding
import com.prestosa.measuremasterchef.room.Recipe

class RecipeAdapter(
    private var recipes: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit,
    private val onDeleteClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(private val binding: RecipeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(recipe: Recipe, onDeleteClick: (Recipe) -> Unit) {
            binding.recipeName.text = recipe.recipeName
            binding.servingSize.text = "Serving Size: ${recipe.servingSize}"

            // Set click listener for the image button
            binding.imageButton.setOnClickListener { view ->
                showPopupMenu(view, recipe, onDeleteClick)
            }
        }

        private fun showPopupMenu(view: View, recipe: Recipe, onDeleteClick: (Recipe) -> Unit) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.delete_menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_delete -> {
                        onDeleteClick(recipe)
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RecipeItemBinding.inflate(inflater, parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.bind(recipe, onDeleteClick)

        // Set click listener for the item
        holder.itemView.setOnClickListener {
            onItemClick(recipe)
        }
    }

    override fun getItemCount(): Int = recipes.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }
}

