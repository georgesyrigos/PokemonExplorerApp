package com.example.pokemonexplorerapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// Response models for individual Pokémon details endpoint
data class PokemonApiResponse(
    val id: Int,
    val name: String,
    val types: List<TypeSlot>,
    val sprites: Sprites,
    val stats: List<StatSlot>
)

data class StatSlot(
    val base_stat: Int,
    val stat: StatName
)

data class StatName(
    val name: String
)

data class TypeSlot(
    val type: TypeName
)

data class TypeName(
    val name: String
)

data class Sprites(
    val front_default: String
)

// Response models for Pokémon type endpoint
data class TypeResponse(
    val pokemon: List<TypePokemonSlot>
)

data class TypePokemonSlot(
    val pokemon: PokemonEntry
)

data class PokemonEntry(
    val name: String,
    val url: String // E.g., "https://pokeapi.co/api/v2/pokemon/4/"
)

interface PokeApiService {

    // 1. Fetch individual Pokémon by ID
    @GET("pokemon/{id}")
    suspend fun getPokemonById(@Path("id") id: Int): PokemonApiResponse

    // 2. Query strictly by Pokémon type (e.g. "fire", "water", "grass")
    @GET("type/{type}")
    suspend fun getPokemonByType(@Path("type") type: String): TypeResponse

    companion object {
        private const val BASE_URL = "https://pokeapi.co/api/v2/"

        fun create(): PokeApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PokeApiService::class.java)
        }
    }
}