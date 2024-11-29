<<<<<<< HEAD

=======
>>>>>>> c86f773 (Reinitialize repository)
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

<<<<<<< HEAD
            // Get streak count from shared preferences (assuming it's saved there)
=======
            // Retrieve the streak count from shared preferences
>>>>>>> c86f773 (Reinitialize repository)
            val prefs = context.getSharedPreferences("JournalAppPrefs", Context.MODE_PRIVATE)
            val streakCount = prefs.getInt("streak_count", 0)
            views.setTextViewText(R.id.widget_streak_count, "Current Streak: $streakCount")

            // Set up the button to open the MainActivity
            val intent = Intent(context, MainActivity::class.java)
<<<<<<< HEAD
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
=======
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
>>>>>>> c86f773 (Reinitialize repository)
            views.setOnClickPendingIntent(R.id.widget_open_app_button, pendingIntent)

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
<<<<<<< HEAD
=======

    override fun onEnabled(context: Context) {
        // Called when the first widget is created
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        // Called when the last widget is removed
        super.onDisabled(context)
    }
>>>>>>> c86f773 (Reinitialize repository)
}