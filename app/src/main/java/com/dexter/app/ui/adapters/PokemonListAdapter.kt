package com.dexter.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dexter.app.R
import com.dexter.app.domain.model.Pokemon

class PokemonListAdapter(
    private val onPokemonClick: (Pokemon) -> Unit
) : ListAdapter<Pokemon, PokemonListAdapter.PokemonViewHolder>(PokemonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view, onPokemonClick)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PokemonViewHolder(
        itemView: View,
        private val onClick: (Pokemon) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.text_pokemon_name)
        private val numberText: TextView = itemView.findViewById(R.id.text_pokemon_number)
        private val iconImage: ImageView = itemView.findViewById(R.id.image_pokemon_sprite)

        fun bind(pokemon: Pokemon) {
            nameText.text = pokemon.name.replaceFirstChar { it.uppercase() }
            numberText.text = String.format("#%03d", pokemon.id)
            itemView.setOnClickListener { onClick(pokemon) }
        }
    }

    private class PokemonDiffCallback : DiffUtil.ItemCallback<Pokemon>() {
        override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean =
            oldItem == newItem
    }
}
