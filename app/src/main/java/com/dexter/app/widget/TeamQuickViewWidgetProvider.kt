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
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class TeamQuickViewWidgetProvider : AppWidgetProvider() {

    private val slotContainerIds = intArrayOf(
        R.id.widget_team_slot_1,
        R.id.widget_team_slot_2,
        R.id.widget_team_slot_3,
        R.id.widget_team_slot_4,
        R.id.widget_team_slot_5,
        R.id.widget_team_slot_6
    )

    private val slotImageIds = intArrayOf(
        R.id.widget_team_img_1,
        R.id.widget_team_img_2,
        R.id.widget_team_img_3,
        R.id.widget_team_img_4,
        R.id.widget_team_img_5,
        R.id.widget_team_img_6
    )

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
            val teamMembers = entryPoint.teamMemberDao().observeTeamMembers().firstOrNull() ?: emptyList()
            val allPokemon = entryPoint.pokemonRepository().observeAllPokemon().firstOrNull() ?: emptyList()
            val pokemonMap = allPokemon.associateBy { it.id }

            for (appWidgetId in appWidgetIds) {
                updateWidgetView(
                    context = context,
                    appWidgetManager = appWidgetManager,
                    appWidgetId = appWidgetId,
                    teamMembers = teamMembers,
                    pokemonMap = pokemonMap
                )
            }
        }
    }

    private suspend fun updateWidgetView(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        teamMembers: List<com.dexter.app.data.local.TeamMemberEntity>,
        pokemonMap: Map<Int, Pokemon>
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_team_quick_view)

        val count = teamMembers.size
        views.setTextViewText(R.id.widget_team_count, "$count/6")

        // Header PendingIntent -> Team Builder
        val teamIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_DESTINATION, MainActivity.DESTINATION_TEAM)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val teamPendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            teamIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_team_container, teamPendingIntent)

        val memberBySlot = teamMembers.associateBy { it.slot }

        for (i in 0 until 6) {
            val slotNumber = i + 1
            val containerId = slotContainerIds[i]
            val imageId = slotImageIds[i]

            val member = memberBySlot[slotNumber]
            val pokemon = member?.let { pokemonMap[it.pokemonId] }

            if (pokemon != null) {
                val spriteUrl = pokemon.spriteUrl.ifBlank { pokemon.officialArtworkUrl ?: "" }
                val bitmap = if (spriteUrl.isNotBlank()) loadBitmap(context, spriteUrl) else null

                if (bitmap != null) {
                    views.setImageViewBitmap(imageId, bitmap)
                } else {
                    views.setImageViewResource(imageId, R.drawable.ic_widget_pokeball)
                }
                views.setInt(containerId, "setBackgroundResource", R.drawable.widget_slot_bg)

                // Click on member slot -> open Detail screen
                val detailIntent = Intent(context, MainActivity::class.java).apply {
                    putExtra(MainActivity.EXTRA_DESTINATION, MainActivity.DESTINATION_DETAIL)
                    putExtra(MainActivity.EXTRA_POKEMON_ID, pokemon.id)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val detailPendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId * 10 + slotNumber,
                    detailIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(containerId, detailPendingIntent)
            } else {
                views.setImageViewResource(imageId, R.drawable.ic_widget_add)
                views.setInt(containerId, "setBackgroundResource", R.drawable.widget_slot_empty_bg)

                // Click on empty slot -> open Team Builder
                views.setOnClickPendingIntent(containerId, teamPendingIntent)
            }
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private suspend fun loadBitmap(context: Context, url: String): Bitmap? {
        return try {
            val imageLoader = Coil.imageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .size(160, 160)
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
}
