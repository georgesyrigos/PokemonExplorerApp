package com.example.pokemonexplorerapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import coil.load


class PokemonDetailsFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pokemon_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imgPokemon: ImageView = view.findViewById(R.id.imgDetailPokemon)
        val tvName: TextView = view.findViewById(R.id.tvDetailName)
        val tvType: TextView = view.findViewById(R.id.tvType)


        val tvHp: TextView = view.findViewById(R.id.tvHp)
        val progressHp: ProgressBar = view.findViewById(R.id.progressHp)

        val tvAttack: TextView = view.findViewById(R.id.tvAttack)
        val progressAttack: ProgressBar = view.findViewById(R.id.progressAttack)

        val tvDefense: TextView = view.findViewById(R.id.tvDefense)
        val progressDefense: ProgressBar = view.findViewById(R.id.progressDefense)



        // Read parcelable passed from list fragment
        val pokemon = arguments?.getParcelable<Pokemon>("selectedPokemon")


        pokemon?.let {
            /// Load Name & Type
            tvName.text = it.name.replaceFirstChar { char -> char.uppercase() }
            tvType.text = "${it.type.replaceFirstChar { char -> char.uppercase() }} Type"


            // 3. Load Sprite Image via Coil
            imgPokemon.load(it.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_foreground)
                error(R.drawable.ic_launcher_foreground)
            }

            // Bind Stat Numbers & Progress Bars
            tvHp.text = "HP: ${it.hp}"
            progressHp.progress = it.hp

            tvAttack.text = "Attack: ${it.attack}"
            progressAttack.progress = it.attack

            tvDefense.text = "Defense: ${it.defense}"
            progressDefense.progress = it.defense


        }
    }


}