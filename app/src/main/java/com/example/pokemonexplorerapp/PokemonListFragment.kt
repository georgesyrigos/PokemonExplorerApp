package com.example.pokemonexplorerapp

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch


class PokemonListFragment : Fragment() {

    private val TAG = "PokemonListFragment"
    private var typeText: String? = null
    private val allPokemonList = mutableListOf<Pokemon>()

    private lateinit var pokemonAdapter: PokemonAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyMessage: TextView
    private lateinit var searchEditText : TextInputEditText

    private val apiService by lazy { PokeApiService.create() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pokemon_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initialize RecyclerView and Adapter
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage)
        recyclerView = view.findViewById(R.id.pokemonRecycler)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        searchEditText = view.findViewById(R.id.searchEditText)

        // Initialize adapter with click lambda
        pokemonAdapter = PokemonAdapter(mutableListOf()) { selectedPokemon ->
            // Handle tap: Navigate to detail fragment or show detail dialog
            openPokemonDetailScreen(selectedPokemon)
        }
        recyclerView.adapter = pokemonAdapter

        searchEditText.doOnTextChanged { text, _, _, _ ->
            filterPokemon(text.toString())
        }

        //2. Keep empty message visible and RecyclerView hidden initially
        tvEmptyMessage.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        // 3. Set up Type Chip Click Listeners
        val typeTextViewIds = listOf(
            R.id.type_1,
            R.id.type_2,
            R.id.type_3,
            R.id.type_4,
            R.id.type_5,
            R.id.type_6,
            R.id.type_7,
            R.id.type_8,
            R.id.type_9,
            R.id.type_10
        )

        typeTextViewIds.forEach { id ->
            val textView = view.findViewById<TextView>(id)

            textView?.setOnClickListener { selectedView ->
                typeTextViewIds.forEach { otherId ->
                    // Dim all textViews to unselected state
                    view.findViewById<TextView>(otherId)?.apply {
                        alpha = 0.4f
                        isSelected = false
                    }
                }

                // Selected textView is brightened
                selectedView.apply {
                    alpha = 1.0f
                    isSelected = true
                }

                // Get selected text and trigger local filter
                val selectedType = (selectedView as TextView).text.toString()
                Log.d(TAG, "Selected Type: $selectedType")
                //added for back navigation from details
                typeText = selectedType
                highlightSelectedTypeChip(view, typeTextViewIds, selectedType)
                fetchPokemonByType(selectedType)
            }
        }
        // Restore state when coming back from Details Screen
        typeText?.let { type ->
            // 1. Re-highlight active type chip
            highlightSelectedTypeChip(view, typeTextViewIds, type)

            // 2. Re-populate adapter from cached master list without network re-query
            if (allPokemonList.isNotEmpty()) {
                tvEmptyMessage.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                pokemonAdapter.updateList(allPokemonList)
            }
        }

    }


    private fun filterPokemon(query: String) {
        val cleanQuery = query.trim().lowercase()

        if (cleanQuery.isEmpty()) {
            // Show all items in current category
            pokemonAdapter.updateList(allPokemonList)
            tvEmptyMessage.visibility = if (allPokemonList.isEmpty()) View.VISIBLE else View.GONE
        } else {
            // Filter master list by name
            val filteredList = allPokemonList.filter { pokemon ->
                pokemon.name.lowercase().contains(cleanQuery)
            }

            pokemonAdapter.updateList(filteredList)

            // Show empty message if query yields no matches
            if (filteredList.isEmpty()) {
                tvEmptyMessage.text = "No Pokémon found matching \"$query\""
                tvEmptyMessage.visibility = View.VISIBLE
            } else {
                tvEmptyMessage.visibility = View.GONE
            }
        }
    }

    // Filters already loaded items without re-querying the network
    private fun fetchPokemonByType(typeName: String) {
        lifecycleScope.launch {
            try {
                // Immediately clear old items so they don't linger on screen
                allPokemonList.clear()
                pokemonAdapter.updateList(allPokemonList)

                // Reset search text box
                searchEditText.text?.clear()

                // Show grid, hide empty state message
                tvEmptyMessage.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                // PokeAPI expects lowercase type names (e.g., "fire", "water")
                val formattedType = typeName.lowercase().trim()

                val typeResponse = apiService.getPokemonByType(formattedType)

                // Extract the first 10 entries of this specific type
                val firstTenEntries = typeResponse.pokemon.take(10)
                val typePokemonList = mutableListOf<Pokemon>()

                for (entry in firstTenEntries) {
                    // Extract ID from resource URL string
                    val pokemonId = entry.pokemon.url
                        .trimEnd('/')
                        .substringAfterLast('/')
                        .toInt()

                    // Fetch full sprite & details for this specific ID
                    val details = apiService.getPokemonById(pokemonId)

                    // Helper function to extract specific stat values
                    fun getStat(name: String): Int {
                        return details.stats.find { it.stat.name.equals(name, ignoreCase = true) }?.base_stat ?: 0
                    }

                    typePokemonList.add(
                        Pokemon(
                            id = details.id,
                            name = details.name,
                            type = typeName,
                            imageUrl = details.sprites.front_default ?: "",
                            hp = getStat("hp"),
                            attack = getStat("attack"),
                            defense = getStat("defense")
                        )
                    )
                }

                // Update master list before giving to adapter
                allPokemonList.clear()
                allPokemonList.addAll(typePokemonList)
                pokemonAdapter.updateList(allPokemonList)

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching type data", e)
            }
        }
    }

    private fun openPokemonDetailScreen(pokemon: Pokemon){
        val bundle = Bundle().apply {
            putParcelable("selectedPokemon", pokemon)
        }

        // Navigate via action ID defined in nav_graph.xml
        findNavController().navigate(
            R.id.action_pokemonListFragment_to_pokemonDetailsFragment,
            bundle
        )
    }


    //added for back navigation from details
    private fun highlightSelectedTypeChip(
        rootView: View,
        chipIds: List<Int>,
        activeTypeName: String
    ) {
        chipIds.forEach { id ->
            val chip = rootView.findViewById<TextView>(id)
            if (chip?.text.toString().equals(activeTypeName, ignoreCase = true)) {
                chip?.alpha = 1.0f
                chip?.isSelected = true
            } else {
                chip?.alpha = 0.4f
                chip?.isSelected = false
            }
        }
    }
}
