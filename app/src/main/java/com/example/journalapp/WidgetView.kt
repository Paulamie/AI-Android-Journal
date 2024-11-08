
package com.example.journalapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class JournalAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // Iterate through all widgets
        for (appWidgetId in appWidgetIds) {
            // Get the widget layout and set up the views
            val views = RemoteViews(context.packageName, R.layout.widget)

            // Get streak count from shared preferences (assuming it's saved there)
            val prefs = context.getSharedPreferences("JournalAppPrefs", Context.MODE_PRIVATE)
            val streakCount = prefs.getInt("streak_count", 0)
            views.setTextViewText(R.id.widget_streak_count, "Current Streak: $streakCount")

            // Set up the button to open the MainActivity
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_open_app_button, pendingIntent)

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}