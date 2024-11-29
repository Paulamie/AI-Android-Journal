package com.example.journalapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.journalapp.databinding.StreakBinding
<<<<<<< HEAD
import java.util.Calendar
=======
import java.text.SimpleDateFormat
import java.util.*
>>>>>>> c86f773 (Reinitialize repository)

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
<<<<<<< HEAD
        }
=======
    }
>>>>>>> c86f773 (Reinitialize repository)

    private fun loadStreakData() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        var streakCount = prefs.getInt(STREAK_COUNT_KEY, 0)
        val lastLoginDate = prefs.getLong(LAST_LOGIN_DATE_KEY, 0)

<<<<<<< HEAD
        val currentDate = Calendar.getInstance().apply { timeInMillis = System.currentTimeMillis() }.get(Calendar.DAY_OF_YEAR)
=======
        val calendar = Calendar.getInstance()
        val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)
>>>>>>> c86f773 (Reinitialize repository)

        if (lastLoginDate != 0L) {
            val lastLoginCalendar = Calendar.getInstance().apply { timeInMillis = lastLoginDate }
            val lastLoginDayOfYear = lastLoginCalendar.get(Calendar.DAY_OF_YEAR)
<<<<<<< HEAD

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
=======
            val lastLoginYear = lastLoginCalendar.get(Calendar.YEAR)

            // Check if it's a new day and update the streak count
            if (currentYear == lastLoginYear && currentDayOfYear == lastLoginDayOfYear + 1) {
                streakCount++ // Consecutive day
            } else if (currentYear != lastLoginYear || currentDayOfYear != lastLoginDayOfYear) {
                streakCount = 1 // Reset streak if days are not consecutive
            }
        } else {
            streakCount = 1 // First-time login or data initialization
        }

        // Save the updated streak count and last login date
        prefs.edit()
            .putInt(STREAK_COUNT_KEY, streakCount)
            .putLong(LAST_LOGIN_DATE_KEY, System.currentTimeMillis())
            .apply()

        // Format last active date
        val lastActiveFormatted = if (lastLoginDate != 0L) {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(lastLoginDate))
        } else {
            "Never"
        }

        // Update UI with the streak data
        binding.streakCountTextView.text = "Current Streak: $streakCount"
        binding.lastActiveDateTextView.text = "Last Active: $lastActiveFormatted"
    }
}
>>>>>>> c86f773 (Reinitialize repository)
