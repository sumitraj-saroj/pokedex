package com.dexter.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.dexter.app.MainActivity
import com.dexter.app.R
import com.dexter.app.domain.model.Pokemon
import com.dexter.app.domain.model.PokemonGeneration
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.random.Random

class PokemonDailyWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "com.dexter.app.widget.ACTION_REFRESH_DAILY_POKEMON"
        private const val PREFS_NAME = "pokemon_daily_widget_prefs"
        private const val KEY_POKEMON_ID_PREFIX = "daily_pokemon_id_"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )

        CoroutineScope(Dispatchers.IO).launch {
            val allPokemon = entryPoint.pokemonRepository().observeAllPokemon().firstOrNull() ?: emptyList()
            if (allPokemon.isEmpty()) return@launch

            for (appWidgetId in appWidgetIds) {
                val pokemon = getOrPickDailyPokemon(context, appWidgetId, allPokemon)
                updateWidgetView(context, appWidgetManager, appWidgetId, pokemon)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )

            CoroutineScope(Dispatchers.IO).launch {
                val allPokemon = entryPoint.pokemonRepository().observeAllPokemon().firstOrNull() ?: emptyList()
                if (allPokemon.isNotEmpty()) {
                    val randomPokemon = allPokemon[Random.nextInt(allPokemon.size)]
                    saveStoredPokemonId(context, appWidgetId, randomPokemon.id)

                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                        updateWidgetView(context, appWidgetManager, appWidgetId, randomPokemon)
                    } else {
                        DexterWidgetManager.updateDailyPokemonWidget(context)
                    }
                }
            }
        }
    }

    private fun getOrPickDailyPokemon(
        context: Context,
        appWidgetId: Int,
        allPokemon: List<Pokemon>
    ): Pokemon {
        val storedId = getStoredPokemonId(context, appWidgetId)
        if (storedId > 0) {
            val found = allPokemon.find { it.id == storedId }
            if (found != null) return found
        }

        // Deterministic daily pick based on day of year
        val dayOfYear = LocalDate.now().dayOfYear
        val index = (dayOfYear * 7 + 13) % allPokemon.size
        val picked = allPokemon[index]
        saveStoredPokemonId(context, appWidgetId, picked.id)
        return picked
    }

    private suspend fun updateWidgetView(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        pokemon: Pokemon
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_pokemon_daily)

        // Text data
        views.setTextViewText(
            R.id.widget_pokemon_name,
            "${pokemon.formattedNumber} ${pokemon.capitalizedName}"
        )

        val typeText = if (pokemon.secondaryType != null) {
            "${pokemon.primaryType.typeName.uppercase()} • ${pokemon.secondaryType.typeName.uppercase()}"
        } else {
            pokemon.primaryType.typeName.uppercase()
        }
        views.setTextViewText(R.id.widget_pokemon_type, typeText)

        val genInfo = PokemonGeneration.fromNumber(pokemon.effectiveGeneration)
        val regionName = genInfo?.regionName ?: "Kanto"
        views.setTextViewText(R.id.widget_pokemon_gen, "Gen ${pokemon.effectiveGeneration} • $regionName")

        val flavorText = if (pokemon.flavorText.isNotBlank()) {
            pokemon.flavorText.replace("\n", " ").replace("\u000c", " ").trim()
        } else if (pokemon.category.isNotBlank()) {
            "${pokemon.category} Pokémon"
        } else {
            "Tap to explore stats, moves, and evolutions in Pokédex."
        }
        views.setTextViewText(R.id.widget_pokemon_flavor, flavorText)

        // Artwork loading
        val artworkUrl = pokemon.officialArtworkUrl ?: pokemon.spriteUrl
        if (artworkUrl.isNotBlank()) {
            val bitmap = loadBitmapFromUrl(context, artworkUrl)
            if (bitmap != null) {
                views.setImageViewBitmap(R.id.widget_pokemon_image, bitmap)
            } else {
                views.setImageViewResource(R.id.widget_pokemon_image, R.drawable.ic_widget_pokeball)
            }
        }

        // Click PendingIntent -> Detail Screen
        val detailIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_DESTINATION, MainActivity.DESTINATION_DETAIL)
            putExtra(MainActivity.EXTRA_POKEMON_ID, pokemon.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val detailPendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            detailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_daily_container, detailPendingIntent)

        // Refresh Button Click -> Broadcast Refresh
        val refreshIntent = Intent(context, PokemonDailyWidgetProvider::class.java).apply {
            action = ACTION_REFRESH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId + 10000,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_daily_refresh, refreshPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private suspend fun loadBitmapFromUrl(context: Context, url: String): Bitmap? {
        return try {
            val imageLoader = Coil.imageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false) // Critical for RemoteViews
                .size(240, 240)
                .build()
            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                (result.drawable as? BitmapDrawable)?.bitmap ?: result.drawable.toBitmap()
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getStoredPokemonId(context: Context, appWidgetId: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_POKEMON_ID_PREFIX + appWidgetId, -1)
    }

    private fun saveStoredPokemonId(context: Context, appWidgetId: Int, pokemonId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_POKEMON_ID_PREFIX + appWidgetId, pokemonId).apply()
    }
}
