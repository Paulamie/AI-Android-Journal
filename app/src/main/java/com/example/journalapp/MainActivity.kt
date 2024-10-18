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

class MainActivity : AppCompatActivity() {

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
}
