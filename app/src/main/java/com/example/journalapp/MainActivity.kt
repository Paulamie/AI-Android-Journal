package com.example.journalapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.io.File
import java.io.InputStream
import java.util.Calendar
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : AppCompatActivity() {

    private var streak = Streak(count = 0, lastDate = 0L) // Initialize streak

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val plusButton: ImageButton = findViewById(R.id.plusButton)

        // Set up the click listener to navigate to NoteDetailActivity
        plusButton.setOnClickListener {
            val intent = Intent(this, NoteDetailActivity::class.java)
            intent.putExtra("NOTE_ID", -1) // -1 means creating a new note
            startActivityForResult(intent, REQUEST_CODE_NOTE)
        }

        reloadNotes()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_NOTE && resultCode == RESULT_OK) {
            // A note was added or updated, reload the notes
            reloadNotes()
            checkAndUpdateStreak() // Check and update the streak after a new note
        }
    }

    companion object {
        private const val REQUEST_CODE_NOTE = 1
    }


    // Reload the notes each time the activity is resumed
    override fun onResume() {
        super.onResume()
        reloadNotes() // Reload the notes when returning from the note detail screen
    }

    // This function handles reloading the notes and updating the RecyclerView
    private fun reloadNotes() {
        val json = loadNotesFromPrivateStorage() ?: loadNotesFromAssets()
        if (json != null) {
            val notes = Gson().fromJson(json, NotesResponse::class.java).notes
            val notesRecyclerView: RecyclerView = findViewById(R.id.notesRecyclerView)
            notesRecyclerView.layoutManager = LinearLayoutManager(this)
            notesRecyclerView.adapter = NotesAdapter(notes) // Reset the adapter with updated notes
        } else {
            Toast.makeText(this, "Failed to load notes data", Toast.LENGTH_SHORT).show()
        }
    }

    // Load notes from private storage
    private fun loadNotesFromPrivateStorage(): String? {
        val fileName = "notes.json"
        val file = File(filesDir, fileName)
        return if (file.exists()) {
            file.readText()  // Return the content of notes.json from private storage
        } else {
            null  // Return null if the file does not exist
        }
    }

    // Load notes from assets as a fallback
    private fun loadNotesFromAssets(): String? {
        return try {
            val inputStream: InputStream = assets.open("notes.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)  // Return the content of notes.json from assets
        } catch (e: Exception) {
            e.printStackTrace()
            null  // Return null if there's an error loading from assets
        }
    }

    // After checking and updating the streak, add a way to navigate to StreakActivity
    private fun checkAndUpdateStreak() {
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        val lastDate = Calendar.getInstance().apply { timeInMillis = streak.lastDate }

        if (today == lastDate.get(Calendar.DAY_OF_YEAR) && streak.lastDate != 0L) {
            // Do nothing, user already created a note today
            return
        } else if (today == lastDate.get(Calendar.DAY_OF_YEAR) + 1) {
            // Increment the streak count if the last note was created yesterday
            streak.count++
        } else {
            // Reset the streak count if there was a break in the streak
            streak.count = 1
        }

        // Update the last date to today's date
        streak.lastDate = currentTime

        // Navigate to StreakActivity when needed
        val intent = Intent(this, StreakActivity::class.java).apply {
            putExtra("STREAK_DATA", streak) // Pass the streak data
        }
        startActivity(intent)
    }
}

@Composable
fun StreakPage(streak: Streak, onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Current Streak: ${streak.count}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Last Active Date: ${formatDate(streak.lastDate)}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp)) // Optional space

        Button(onClick = { onNavigateBack() }) { // Use Button from Compose
            Text("Back")
        }
    }
}

// Helper function to format the last active date
fun formatDate(timestamp: Long): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
    return "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
}