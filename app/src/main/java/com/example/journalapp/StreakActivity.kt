package com.example.journalapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.journalapp.databinding.StreakBinding
import java.text.SimpleDateFormat
import java.util.*

class StreakActivity : AppCompatActivity() {

    private lateinit var binding: StreakBinding

    // Constants for SharedPreferences keys
    private val PREFS_NAME = "StreakPrefs"
    private val STREAK_COUNT_KEY = "streak_count"
    private val LAST_LOGIN_DATE_KEY = "last_login_date"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding
        binding = StreakBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load streak data and update the UI
        loadStreakData()
    }

    private fun loadStreakData() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        var streakCount = prefs.getInt(STREAK_COUNT_KEY, 0)
        val lastLoginDate = prefs.getLong(LAST_LOGIN_DATE_KEY, 0)

        // Get the current date information
        val calendar = Calendar.getInstance()
        val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        if (lastLoginDate != 0L) {
            // Convert the last login timestamp to a Calendar instance
            val lastLoginCalendar = Calendar.getInstance().apply { timeInMillis = lastLoginDate }
            val lastLoginDayOfYear = lastLoginCalendar.get(Calendar.DAY_OF_YEAR)
            val lastLoginYear = lastLoginCalendar.get(Calendar.YEAR)

            // Check streak conditions
            if (currentYear == lastLoginYear && currentDayOfYear == lastLoginDayOfYear + 1) {
                streakCount++ // Increment streak for consecutive day
            } else if (currentYear != lastLoginYear || currentDayOfYear != lastLoginDayOfYear) {
                streakCount = 1 // Reset streak if not consecutive
            }
        } else {
            streakCount = 1 // First-time login or no previous data
        }

        // Save updated streak data to SharedPreferences
        prefs.edit()
            .putInt(STREAK_COUNT_KEY, streakCount)
            .putLong(LAST_LOGIN_DATE_KEY, System.currentTimeMillis())
            .apply()

        // Format the last active date
        val lastActiveFormatted = if (lastLoginDate != 0L) {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(lastLoginDate))
        } else {
            "Never"
        }

        // Update the UI
        binding.streakCountTextView.text = "Current Streak: $streakCount"
        binding.lastActiveDateTextView.text = "Last Active: $lastActiveFormatted"
    }
}
