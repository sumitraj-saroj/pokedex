package com.dexter.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object DexterWidgetManager {

    fun updateAllWidgets(context: Context) {
        updateDailyPokemonWidget(context)
        updateQuizStreakWidget(context)
        updateTeamQuickViewWidget(context)
    }

    fun updateDailyPokemonWidget(context: Context) {
        val intent = Intent(context, PokemonDailyWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, PokemonDailyWidgetProvider::class.java))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(intent)
    }

    fun updateQuizStreakWidget(context: Context) {
        val intent = Intent(context, QuizStreakWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, QuizStreakWidgetProvider::class.java))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(intent)
    }

    fun updateTeamQuickViewWidget(context: Context) {
        val intent = Intent(context, TeamQuickViewWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, TeamQuickViewWidgetProvider::class.java))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(intent)
    }
}
