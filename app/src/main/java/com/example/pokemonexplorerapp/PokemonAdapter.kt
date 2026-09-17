package com.example.pokemonexplorerapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class PokemonAdapter(
    private var pokemonList: List<Pokemon>,
    private val onItemClick: (Pokemon) -> Unit
): RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder> (){

    // 1. ViewHolder: Holds references to the item views inside item_pokemon_card.xml
    class PokemonViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imgPokemon: ImageView = itemView.findViewById(R.id.pokemonImage)
        val tvPokemonName: TextView = itemView.findViewById(R.id.pokemonName)
    }

    // 2. Inflates the layout file (item_pokemon_card.xml) for each grid card
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pokemon_card, parent, false)
        return PokemonViewHolder(view)
    }


    // 3. Binds data from a Pokemon item to the TextView and ImageView
    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = pokemonList[position]

        // Capitalize first letter of name
        holder.tvPokemonName.text = pokemon.name.replaceFirstChar { it.uppercase() }

        // Load remote image URL using Coil
        holder.imgPokemon.load(pokemon.imageUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_foreground) // optional placeholder
        }

        // Handle Card Click
        holder.itemView.setOnClickListener {
            onItemClick(pokemon)
        }
    }

    // 4. Returns total number of items
    override fun getItemCount(): Int = pokemonList.size

    // 5. Helper function to refresh data when filtering
    fun updateList(newList: List<Pokemon>) {
        pokemonList = newList
        notifyDataSetChanged()
    }
}
