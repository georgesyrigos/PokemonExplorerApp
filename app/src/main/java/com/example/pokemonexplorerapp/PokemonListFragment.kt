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
import androidx.core.view.isGone
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
    private var formList : List<TypePokemonSlot> = emptyList()
    private var countedItems = 0
    private lateinit var loadMoreBtn : Button

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

        // Initialize RecyclerView and Adapter
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage)
        recyclerView = view.findViewById(R.id.pokemonRecycler)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        searchEditText = view.findViewById(R.id.searchEditText)

        // Initialize load more button
        loadMoreBtn = view.findViewById(R.id.btnLoadMore)
        loadMoreBtn.setOnClickListener {
            typeText?.let { type ->
                loadNewEntries(type)
            }
        }

        // Initialize adapter with click lambda
        pokemonAdapter = PokemonAdapter(mutableListOf()) { selectedPokemon ->
            // Handle tap: Navigate to detail fragment or show detail dialog
            openPokemonDetailScreen(selectedPokemon)
        }
        recyclerView.adapter = pokemonAdapter

        searchEditText.doOnTextChanged { text, _, _, _ ->
            filterPokemon(text.toString())
        }

        // Keep empty message visible and RecyclerView hidden initially
        tvEmptyMessage.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        // Set up Type Chip Click Listeners
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
                // Added for back navigation from details
                typeText = selectedType
                highlightSelectedTypeChip(view, typeTextViewIds, selectedType)
                fetchPokemonByType(selectedType)
            }
        }
        // Restore state when coming back from Details Screen
        typeText?.let { type ->
            // Re-highlight active type chip
            highlightSelectedTypeChip(view, typeTextViewIds, type)

            // Re-populate adapter from cached master list without network re-query
            if (allPokemonList.isNotEmpty()) {
                tvEmptyMessage.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                pokemonAdapter.updateList(allPokemonList)

                // Restore 'Load More' button visibility state upon return
                if (formList.isNotEmpty() && countedItems < formList.size) {
                    loadMoreBtn.visibility = View.VISIBLE
                    loadMoreBtn.isEnabled = true
                    loadMoreBtn.text = "Load More"
                } else {
                    loadMoreBtn.visibility = View.GONE
                }
            }
        }

    }


    private fun filterPokemon(query: String) {
        val cleanQuery = query.trim().lowercase()

        if (cleanQuery.isEmpty()) {
            // Show all items in current category
            pokemonAdapter.updateList(allPokemonList)
            tvEmptyMessage.visibility = if (allPokemonList.isEmpty()) View.VISIBLE else View.GONE

            if (formList.isNotEmpty() && countedItems < formList.size) {
                loadMoreBtn.visibility = View.VISIBLE
                loadMoreBtn.isEnabled = true
                loadMoreBtn.text = "Load More"
            } else {
                loadMoreBtn.visibility = View.GONE
            }
        } else {
            loadMoreBtn.visibility = View.GONE

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
                countedItems = 0
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

                formList = typeResponse.pokemon
                if (formList.isEmpty()) {
                    loadMoreBtn.visibility = View.GONE
                    showNoPokemonAlert("No Pokémon found for $typeName type.")
                    return@launch
                }

                loadMoreBtn.visibility = View.VISIBLE
                loadNewEntries(typeName)

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching type data", e)
                showNoPokemonAlert("Failed to load $typeName Pokémon. Please check your internet connection and try again.")
            }
        }
    }

    //Load pokemon
    private fun  loadNewEntries(typeName: String){
        lifecycleScope.launch {
            try {
                if(countedItems>=formList.size){
                    loadMoreBtn.visibility = View.GONE
                    showNoPokemonAlert("No more Pokémon available for $typeName type.")
                    return@launch
                }

                loadMoreBtn.isEnabled = false
                loadMoreBtn.text = "Loading..."

                val nextTenEnries = formList.drop(countedItems).take(10)

                for (entry in nextTenEnries) {
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

                    allPokemonList.add(
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
                countedItems += nextTenEnries.size
                pokemonAdapter.updateList(allPokemonList)

                // Hide button if we reached the absolute end of the type list
                if (countedItems >= formList.size) {
                    loadMoreBtn.visibility = View.GONE
                } else {
                    loadMoreBtn.visibility = View.VISIBLE
                    loadMoreBtn.isEnabled = true
                    loadMoreBtn.text = "Load More"
                }
            }
            catch (e: Exception) {
                loadMoreBtn.isEnabled = true
                loadMoreBtn.text = "Load More"
                showNoPokemonAlert("Failed to load additional Pokémon. Please check your network connection.")
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

    // Show Message bto inform the user about error
    private fun showNoPokemonAlert(message: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Notice")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }


    // Added for back navigation from details
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
