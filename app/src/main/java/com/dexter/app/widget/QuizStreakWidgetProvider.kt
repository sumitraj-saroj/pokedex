package com.dexter.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.dexter.app.MainActivity
import com.dexter.app.R
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class QuizStreakWidgetProvider : AppWidgetProvider() {

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
            val trainerData = entryPoint.trainerPreferencesRepository().trainerDataFlow.firstOrNull()
            val allScores = entryPoint.quizScoreDao().observeAllQuizScores().firstOrNull() ?: emptyList()

            val level = trainerData?.level ?: 1
            val streak = trainerData?.loginStreak ?: 1
            val bestQuizStreak = allScores.maxOfOrNull { it.bestStreak } ?: 0
            val totalCorrect = allScores.sumOf { it.correctCount }

            for (appWidgetId in appWidgetIds) {
                updateWidgetView(
                    context = context,
                    appWidgetManager = appWidgetManager,
                    appWidgetId = appWidgetId,
                    level = level,
                    streak = streak,
                    bestStreak = bestQuizStreak,
                    totalCorrect = totalCorrect
                )
            }
        }
    }

    private fun updateWidgetView(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        level: Int,
        streak: Int,
        bestStreak: Int,
        totalCorrect: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_quiz_streak)

        views.setTextViewText(R.id.widget_quiz_level, "Lv. $level")
        views.setTextViewText(R.id.widget_quiz_streak, "$streak DAY STREAK")
        views.setTextViewText(R.id.widget_quiz_stats, "Best Streak: $bestStreak • $totalCorrect Correct")

        // Click PendingIntent -> Quiz Screen
        val quizIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_DESTINATION, MainActivity.DESTINATION_QUIZ)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val quizPendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            quizIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(R.id.widget_quiz_container, quizPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_quiz_play_btn, quizPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
