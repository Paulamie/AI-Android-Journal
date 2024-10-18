package com.example.journalapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.journalapp.databinding.ActivityNoteDetailBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.InputStream
import android.view.View


data class QuestionsWrapper(val questions: List<String>)

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailBinding
    private var notes: MutableList<Note> = mutableListOf()
    private var questions: List<String> = listOf()
    private var currentIndex = -1 // Use -1 as a flag for a new note

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadNotes()
        loadQuestions()

        binding.questionButton.setOnClickListener {
            displayRandomQuestion()
            binding.confirmQuestionButton.visibility = View.VISIBLE
        }

        binding.confirmQuestionButton.setOnClickListener {
            confirmQuestion()
        }

        binding.btnSave.setOnClickListener {
            if (currentIndex == -1) {
                addNote()
            } else {
                updateNote()
            }
        }
    }


    private fun loadNotes() {
        val jsonString = loadNotesFromPrivateStorage()
        if (jsonString != null) {
            val type = object : TypeToken<NotesResponse>() {}.type
            val data = Gson().fromJson<NotesResponse>(jsonString, type)
            notes = data.notes.toMutableList()
        }
    }



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
    private fun displayRandomQuestion() {
        if (questions.isNotEmpty()) {
            val randomIndex = questions.indices.random()
            binding.questionText.text = questions[randomIndex]
        } else {
            Toast.makeText(this, "No questions available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmQuestion() {
        binding.etNoteTitle.setText(binding.questionText.text)
    }

    private fun addNote() {
        val newTitle = binding.etNoteTitle.text.toString()
        val newContent = binding.etNoteContent.text.toString()
        notes.add(Note(newTitle, newContent))
        saveNotesToFile()
        setResult(RESULT_OK)
        Toast.makeText(this, "New note added", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun updateNote() {
        val updatedTitle = binding.etNoteTitle.text.toString()
        val updatedContent = binding.etNoteContent.text.toString()
        if (currentIndex != -1 && currentIndex < notes.size) {
            notes[currentIndex] = notes[currentIndex].copy(title = updatedTitle, content = updatedContent)
            saveNotesToFile()
            setResult(RESULT_OK)
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error updating note", Toast.LENGTH_SHORT).show()
        }
    }

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
