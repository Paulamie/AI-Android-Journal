//package com.example.journalapp
//
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import com.example.journalapp.databinding.ActivityNoteDetailBinding
//
//class NoteDetailActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityNoteDetailBinding
//    private var noteId: Int? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        noteId = intent.getIntExtra("NOTE_ID", -1)
//
//        noteId?.let {
//            if (it != -1) {
//                loadNoteDetails(it)
//            }
//        }
//
//        binding.btnSave.setOnClickListener {
//            saveNote()
//        }
//    }
//
//    private fun loadNoteDetails(noteId: Int) {
//        // Load note details from database
//    }
//
//    private fun saveNote() {
//        val title = binding.etNoteTitle.text.toString()
//        val content = binding.etNoteContent.text.toString()
//        // Save the note to the database
//    }
//}


package com.example.journalapp

import android.os.Bundle
import android.widget.Toast // Add this import for Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.journalapp.databinding.ActivityNoteDetailBinding

class NoteDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteDetailBinding
    private var noteId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the note ID from the Intent
        noteId = intent.getIntExtra("NOTE_ID", -1)

        // Check if it's a new note or an existing one
        if (noteId != -1) {
            loadNoteDetails(noteId!!)
        }

        // Save the note when the save button is clicked
        binding.btnSave.setOnClickListener {
            saveNote()
        }
    }

    // Function to load note details if the note exists
    private fun loadNoteDetails(noteId: Int) {
        // Here you would load the note details for editing
        binding.etNoteTitle.setText("Existing Note Title")
        binding.etNoteContent.setText("Existing Note Content")
    }

    // Function to save a new note or update an existing note
    private fun saveNote() {
        val title = binding.etNoteTitle.text.toString()
        val content = binding.etNoteContent.text.toString()
        if (noteId == -1) {
            // Handle the logic for saving a new note
            Toast.makeText(this, "New note saved", Toast.LENGTH_SHORT).show() // Toast message for new note
        } else {
            // Handle the logic for updating an existing note
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show() // Toast message for updating a note
        }
        finish() // Close the activity after saving
    }
}

