package com.example.journalapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class JournalAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Iterate through all widget instances
        for (appWidgetId in appWidgetIds) {
            // Inflate the widget layout
            val views = RemoteViews(context.packageName, R.layout.widget)

            // Retrieve streak count from SharedPreferences
            val prefs = context.getSharedPreferences("JournalAppPrefs", Context.MODE_PRIVATE)
            val streakCount = prefs.getInt("streak_count", 0)
            views.setTextViewText(R.id.widget_streak_count, "Current Streak: $streakCount")

            // Set up an Intent to open the MainActivity when the button is clicked
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_open_app_button, pendingIntent)

            // Update the widget with the new data
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onEnabled(context: Context) {
        // Called when the first widget is created
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        // Called when the last widget is removed
        super.onDisabled(context)
    }
}
