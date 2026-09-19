# ⚡ Pokémon Explorer Android App

An Android application built with **Kotlin**, **Jetpack Components**, and **PokeAPI**. The app allows users to explore Pokémon by type categories, perform real-time name searches, view detailed stats, and lazily load additional data with asynchronous pagination.

---

## 📱 Features

- **Category Filtering:** Filter Pokémon dynamically by type (Fire, Water, Grass, etc.) with custom visual tab highlights.
- **Real-Time Search:** Instant filtering of cached Pokémon lists using `TextInputEditText`.
- **Stat Visualizations:** View detailed stat breakdowns (HP, Attack, Defense, Speed) with animated `ProgressBar` indicators.
- **Asynchronous Pagination:** Load data in batches of 10 items concurrently using Kotlin Coroutines (`async`/`awaitAll`).
- **Resilient Navigation:** Preserves selected category state and loaded list items across `Fragment` back-stack transitions via Jetpack Navigation.
- **Locked Orientation:** Portrait-mode design optimized for uniform vertical scrolling and responsive grid layouts.

---

## 🛠️ Tech Stack & Libraries

* **Language:** Kotlin
* **Architecture:** Single Activity architecture using Jetpack Navigation Component
* **Networking:** Retrofit 2 + Gson Converter
* **Image Loading:** Coil (Coroutines Image Loader)
* **Async & Concurrency:** Kotlin Coroutines & Flow
* **UI Layouts:** ConstraintLayout, RecyclerView with GridLayoutManager, Material Design Components

---

## 🧱 Architecture Overview

```text
app/
 ├── data/
 │    ├── api/           # Retrofit Interface & API Service Definition
 │    └── model/         # Data Classes & Parcelable Data Transfer Objects (Pokemon)
 ├── ui/
 │    ├── PokemonListFragment.kt     # Master view with filtering & load-more pagination
 │    ├── PokemonDetailsFragment.kt  # Stat detail view & navigation handling
 │    └── PokemonAdapter.kt          # RecyclerView Adapter for grid rendering
 └── MainActivity.kt                 # Navigation host container
