# Pokémon Explorer Android App

An Android application built with **Kotlin**, **Jetpack Components**, and **PokeAPI**. The app allows users to explore Pokémon by type categories, perform real-time name searches, view detailed stats, and lazily load additional data with asynchronous pagination.

---

## Features

- **Category Filtering:** Filter Pokémon dynamically by type (Fire, Water, Grass, etc.) with custom visual tab highlights.
- **Real-Time Search:** Instant filtering of cached Pokémon lists using `TextInputEditText`.
- **Stat Visualizations:** View detailed stat breakdowns (HP, Attack, Defense, Speed) with animated `ProgressBar` indicators.
- **Asynchronous Pagination:** Load data in batches of 10 items concurrently using Kotlin Coroutines (`async`/`awaitAll`).
- **Resilient Navigation:** Preserves selected category state and loaded list items across `Fragment` back-stack transitions via Jetpack Navigation.
- **Locked Orientation:** Portrait-mode design optimized for uniform vertical scrolling and responsive grid layouts.

---

## Tech Stack & Libraries

* **Language:** Kotlin
* **Architecture:** Single Activity architecture using Jetpack Navigation Component
* **Networking:** Retrofit 2 + Gson Converter
* **Image Loading:** Coil (Coroutines Image Loader)
* **Async & Concurrency:** Kotlin Coroutines & Flow
* **UI Layouts:** ConstraintLayout, RecyclerView with GridLayoutManager, Material Design Components

---

## Core Architecture Components

* **UI Layer (`Fragment` + `RecyclerView`):**
  * `PokemonListFragment`: Manages category selection, search text input filtering, state restoration, and pagination controls.
  * `PokemonDetailsFragment`: Renders high-resolution sprites and maps base stats (`HP`, `Attack`, `Defense`) onto progress indicators.
* **Data Layer (`Model` & `Network`):**
  * **PokeAPI Service:** Handles Retrofit REST endpoints for fetching type categories and individual Pokémon detail objects asynchronously.
  * **Local Master Cache (`allPokemonList`):** Holds the current category's active items in memory to allow instant UI restoration when returning from detail screens without redundant network queries.
* **Navigation Component:**
  * Handles fragment transitions and safely passes `Parcelable` Pokémon data models across the back-stack.
