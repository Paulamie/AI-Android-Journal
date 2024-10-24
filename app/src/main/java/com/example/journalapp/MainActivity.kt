package com.example.journalapp

import android.content.Intent
import android.os.Bundle
import android.view.View  // Import for controlling view visibility
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.journalapp.databinding.ActivityMainBinding
import com.google.gson.Gson
import java.io.File
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isInSelectionMode = false
    private val selectedNotes = mutableListOf<Note>() // List of selected notes for deletion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        reloadNotes()

        // Floating action button for adding a new note
        binding.plusButton.setOnClickListener {
            if (!isInSelectionMode) {
                val intent = Intent(this, NoteDetailActivity::class.java)
                intent.putExtra("NOTE_ID", -1) // -1 means creating a new note
                startActivityForResult(intent, REQUEST_CODE_NOTE)
            }
        }

        // Delete button for deleting selected notes
        binding.deleteButton.setOnClickListener {
            deleteSelectedNotes()
        }
    }

    // Reload notes into the RecyclerView
    private fun reloadNotes() {
        val json = loadNotesFromPrivateStorage() ?: loadNotesFromAssets()
        if (json != null) {
            val notes = Gson().fromJson(json, NotesResponse::class.java).notes.toMutableList()
            val notesRecyclerView: RecyclerView = binding.notesRecyclerView
            notesRecyclerView.layoutManager = LinearLayoutManager(this)

            notesRecyclerView.adapter = NotesAdapter(notes, { position ->
                if (isInSelectionMode) {
                    toggleSelection(position)
                } else {
                    val intent = Intent(this, NoteDetailActivity::class.java)
                    intent.putExtra("NOTE_ID", position)
                    startActivityForResult(intent, REQUEST_CODE_NOTE)
                }
            }, { position ->
                isInSelectionMode = true
                toggleSelection(position)
            })
        } else {
            Toast.makeText(this, "Failed to load notes data", Toast.LENGTH_SHORT).show()
        }
    }

    // Toggle the selection of a note
    private fun toggleSelection(position: Int) {
        val adapter = binding.notesRecyclerView.adapter as NotesAdapter
        val note = adapter.notes[position]
        note.isSelected = !note.isSelected

        if (note.isSelected) {
            selectedNotes.add(note)
        } else {
            selectedNotes.remove(note)
        }

        adapter.notifyItemChanged(position)

        // Show or hide the trash icon based on the selection
        if (selectedNotes.isEmpty()) {
            isInSelectionMode = false
            binding.deleteButton.visibility = View.GONE // Hide trash icon when nothing is selected
        } else {
            isInSelectionMode = true
            binding.deleteButton.visibility = View.VISIBLE // Show trash icon when items are selected
        }
    }

    // Delete the selected notes
    private fun deleteSelectedNotes() {
        val adapter = binding.notesRecyclerView.adapter as NotesAdapter
        adapter.notes.removeAll(selectedNotes)
        adapter.notifyDataSetChanged()

        // Clear selection mode and hide the trash icon
        selectedNotes.clear()
        isInSelectionMode = false
        binding.deleteButton.visibility = View.GONE

        // Save the updated list to storage
        saveNotesToFile(adapter.notes)
    }

    // Handle returning from NoteDetailActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_NOTE && resultCode == RESULT_OK) {
            // Reload the notes after adding or updating a note
            reloadNotes()
        }
    }

    // Load notes from private storage (if exists)
    private fun loadNotesFromPrivateStorage(): String? {
        val file = File(filesDir, "notes.json")
        return if (file.exists()) file.readText() else null
    }

    // Load notes from assets if private storage doesn't have any
    private fun loadNotesFromAssets(): String? {
        return try {
            val inputStream: InputStream = assets.open("notes.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Save the notes back to the file
    private fun saveNotesToFile(notes: List<Note>) {
        val notesResponse = NotesResponse(notes)
        val json = Gson().toJson(notesResponse)
        val file = File(filesDir, "notes.json")
        try {
            file.writeText(json)
            Toast.makeText(this, "Notes saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving notes", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_CODE_NOTE = 1
    }
}


////    private fun checkAndUpdateStreak() {
////        val currentTime = System.currentTimeMillis()
////        val calendar = Calendar.getInstance()
////        calendar.timeInMillis = currentTime
////        val today = calendar.get(Calendar.DAY_OF_YEAR)
////        val lastDate = Calendar.getInstance().apply { timeInMillis = streak.lastDate }
////
////        if (today == lastDate.get(Calendar.DAY_OF_YEAR) && streak.lastDate != 0L) {
////            return
////        } else if (today == lastDate.get(Calendar.DAY_OF_YEAR) + 1) {
////            streak.count++
////        } else {
////            streak.count = 1
////        }
////
////        streak.lastDate = currentTime
////
////        val intent = Intent(this, StreakActivity::class.java).apply {
////            putExtra("STREAK_DATA", streak)
////        }
////        startActivity(intent)
////    }
////}


