package com.example.journalapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.journalapp.databinding.StreakBinding
import java.util.Calendar

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

        val currentDate = Calendar.getInstance().apply { timeInMillis = System.currentTimeMillis() }.get(Calendar.DAY_OF_YEAR)

        if (lastLoginDate != 0L) {
            val lastLoginCalendar = Calendar.getInstance().apply { timeInMillis = lastLoginDate }
            val lastLoginDayOfYear = lastLoginCalendar.get(Calendar.DAY_OF_YEAR)

            // Check if it's a new day to update the streak count
            if (currentDate == lastLoginDayOfYear + 1) {
                streakCount++ // New day, increment streak
            } else if (currentDate != lastLoginDayOfYear) {
                streakCount = 1 // Reset streak if the last login was not consecutive
            }
            } else {
                streakCount = 1 // First-time login or data initialization
            }

        // Save the updated streak count and last login date
        prefs.edit().putInt(STREAK_COUNT_KEY, streakCount).apply()
        prefs.edit().putLong(LAST_LOGIN_DATE_KEY, System.currentTimeMillis()).apply()

        // Update UI with the streak data
        binding.streakCountTextView.text = "Current Streak: $streakCount"
        binding.lastActiveDateTextView.text = "Last Active: ${Calendar.getInstance().apply { timeInMillis = lastLoginDate }.time}"
        }
    }