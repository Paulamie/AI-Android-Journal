package com.example.journalapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.ui.tooling.preview.Preview

class StreakActivity : ComponentActivity() {
    private lateinit var streak: Streak

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the streak data passed from the intent
        streak = intent.getSerializableExtra("STREAK_DATA") as? Streak ?: Streak(0, 0L)

//        setContent {
//            StreakPage(streak = streak) {
//                // Handle navigation back to the main activity
//                finish() // Close the StreakActivity and go back to the previous one
//            }
        }
    }

