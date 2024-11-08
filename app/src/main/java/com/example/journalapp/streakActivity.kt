package com.example.journalapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.journalapp.databinding.StreakBinding
import java.util.Calendar

class StreakActivity : AppCompatActivity() {

    private lateinit var binding: StreakBinding

    // Variables to hold streak data
    private var streakCount = 0
    private var lastActiveDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding
        binding = StreakBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load streak data from Shared Preferences
        loadStreakData()
        updateStreakUI()  // Update the UI with loaded data

        // Example of handling a button click
        binding.viewDetailsButton.setOnClickListener {
            // Handle button click (e.g., show details or navigate to another activity)
        }
    }

    // Load streak data from Shared Preferences
    private fun loadStreakData() {
        val prefs = getSharedPreferences("JournalAppPrefs", MODE_PRIVATE)
        streakCount = prefs.getInt("streak_count", 0)
        lastActiveDate = prefs.getLong("last_active_date", 0)
    }

    // Update the UI with streak information
    private fun updateStreakUI() {
        binding.streakCountTextView.text = "Current Streak: $streakCount"
        val lastDate = if (lastActiveDate == 0L) "Never" else Calendar.getInstance().apply { timeInMillis = lastActiveDate }.time.toString()
        binding.lastActiveDateTextView.text = "Last Active: $lastDate"
    }
}