package com.example.journalapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.journalapp.databinding.ActivityNoteDetailBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.InputStream
import com.example.journalapp.Note
import com.example.journalapp.NotesResponse


class NoteDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteDetailBinding
    private var notes: MutableList<Note> = mutableListOf()
    private var currentIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notes = loadNotes()  // Load existing notes from storage

        // Intents could pass the index of the note to be edited
        currentIndex = intent.getIntExtra("note_index", -1)

        if (currentIndex != -1 && currentIndex < notes.size) {
            loadNoteDetails(currentIndex)  // Load note details for editing
        }

        binding.btnSave.setOnClickListener {
            if (currentIndex == -1) {
                addNote()  // Add new note
            } else {
                updateNote()  // Update existing note
            }
        }
    }

    private fun loadNoteDetails(index: Int) {
        binding.etNoteTitle.setText(notes[index].title)
        binding.etNoteContent.setText(notes[index].content)
    }

    private fun loadNotes(): MutableList<Note> {
        val fileName = "notes.json"
        val file = File(filesDir, fileName)
        val json: String = if (file.exists()) {
            file.readText()
        } else {
            val inputStream: InputStream = assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        }

        val notesResponse = Gson().fromJson(json, NotesResponse::class.java)
        return notesResponse.notes.toMutableList()
    }

    private fun addNote() {
        val newTitle = binding.etNoteTitle.text.toString()
        val newContent = binding.etNoteContent.text.toString()
        notes.add(Note(newTitle, newContent))
        saveNotesToFile()
        Toast.makeText(this, "New note added", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun updateNote() {
        val updatedTitle = binding.etNoteTitle.text.toString()
        val updatedContent = binding.etNoteContent.text.toString()
        if (currentIndex != -1 && currentIndex < notes.size) {
            notes[currentIndex] = notes[currentIndex].copy(title = updatedTitle, content = updatedContent)
            saveNotesToFile()
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error updating note", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveNotesToFile() {
        val notesResponse = NotesResponse(notes)
        val json = Gson().toJson(notesResponse)
        val fileName = "notes.json"
        val file = File(filesDir, fileName)
        file.writeText(json)
    }
}
