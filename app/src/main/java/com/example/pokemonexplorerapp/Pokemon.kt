package com.example.pokemonexplorerapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Pokemon(
    val id: Int,
    val name: String,
    val type: String,
    val imageUrl: String,
    val hp: Int = 0,
    val attack: Int = 0,
    val defense: Int = 0,
) : Parcelable

