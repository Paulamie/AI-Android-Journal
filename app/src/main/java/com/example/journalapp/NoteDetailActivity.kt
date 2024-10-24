package com.example.journalapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.journalapp.databinding.ActivityNoteDetailBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.InputStream


data class QuestionsWrapper(val questions: List<String>)

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailBinding
    private var notes: MutableList<Note> = mutableListOf()
    private var questions: List<String> = listOf()
    private var currentIndex = -1 // Default to -1 for new notes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadNotes()
        loadQuestions() // Load questions from the JSON file

        // Show a random question when the question button is clicked
        binding.questionButton.setOnClickListener {
            displayRandomQuestion()
        }

        // Confirm the question as the note title
        binding.confirmQuestionButton.setOnClickListener {
            confirmQuestion()
        }

        // Get the note ID from the intent, so we know if we're editing an existing note
        currentIndex = intent.getIntExtra("NOTE_ID", -1)
        if (currentIndex != -1) {
            loadNoteForEditing(currentIndex)
        }

        // Save button click listener
        binding.btnSave.setOnClickListener {
            if (currentIndex == -1) {
                addNote()
            } else {
                updateNote()
            }
        }
    }

    // Load the notes for editing
    private fun loadNoteForEditing(noteId: Int) {
        if (noteId >= 0 && noteId < notes.size) {
            val note = notes[noteId]
            binding.etNoteTitle.setText(note.title)
            binding.etNoteContent.setText(note.content)
        }
    }

    // Load the questions from assets/journal_questions.json
    private fun loadQuestions() {
        try {
            val inputStream: InputStream = assets.open("journal_questions.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val typeQuestions = object : TypeToken<QuestionsWrapper>() {}.type
            questions = Gson().fromJson<QuestionsWrapper>(jsonString, typeQuestions).questions
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading questions: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Display a random question from the list
    private fun displayRandomQuestion() {
        if (questions.isNotEmpty()) {
            val randomIndex = questions.indices.random()
            binding.questionText.text = questions[randomIndex]
            binding.confirmQuestionButton.visibility = View.VISIBLE // Show the confirm button
        } else {
            Toast.makeText(this, "No questions available", Toast.LENGTH_SHORT).show()
        }
    }

    // Confirm the random question as the note title
    private fun confirmQuestion() {
        binding.etNoteTitle.setText(binding.questionText.text)
        binding.confirmQuestionButton.visibility = View.GONE // Hide the confirm button after confirming
    }

    // Add a new note
    private fun addNote() {
        val newTitle = binding.etNoteTitle.text.toString()
        val newContent = binding.etNoteContent.text.toString()
        notes.add(Note(newTitle, newContent))
        saveNotesToFile()
        setResult(RESULT_OK) // Notify MainActivity that a note was added
        finish() // Close the activity and return to MainActivity
    }

    // Update an existing note
    private fun updateNote() {
        val updatedTitle = binding.etNoteTitle.text.toString()
        val updatedContent = binding.etNoteContent.text.toString()
        if (currentIndex != -1 && currentIndex < notes.size) {
            notes[currentIndex] = notes[currentIndex].copy(title = updatedTitle, content = updatedContent)
            saveNotesToFile()
            setResult(RESULT_OK) // Notify MainActivity that a note was updated
            finish() // Close the activity and return to MainActivity
        } else {
            Toast.makeText(this, "Error updating note", Toast.LENGTH_SHORT).show()
        }
    }

    // Save notes to a JSON file in private storage
    private fun saveNotesToFile() {
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

    // Load notes from private storage
    private fun loadNotes() {
        val jsonString = loadNotesFromPrivateStorage()
        if (jsonString != null) {
            val data = Gson().fromJson(jsonString, NotesResponse::class.java)
            notes = data.notes.toMutableList()
        }
    }

    // Helper method to load the notes from private storage
    private fun loadNotesFromPrivateStorage(): String? {
        val fileName = "notes.json"
        val file = File(filesDir, fileName)
        return if (file.exists()) {
            file.readText()
        } else {
            null
        }
    }
}
