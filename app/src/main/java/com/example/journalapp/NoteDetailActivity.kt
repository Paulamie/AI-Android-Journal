package com.example.journalapp

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.example.journalapp.databinding.ActivityNoteDetailBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.InputStream
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.*
import java.lang.reflect.Type




data class QuestionsWrapper(val questions: List<String>)

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailBinding
    private var notes: MutableList<Note> = mutableListOf()
    private var questions: List<String> = emptyList()
    private var currentIndex = -1
    private var selectedFileUri: Uri? = null

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                selectedFileUri = it
                val fileType = contentResolver.getType(it)

                if (fileType != null) {
                    displayFilePreview(it, fileType)
                } else {
                    Toast.makeText(this, "Unsupported file type", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadNotes()
        loadQuestions()

        binding.questionButton.setOnClickListener { displayRandomQuestion() }
        binding.confirmQuestionButton.setOnClickListener { confirmQuestion() }

        currentIndex = intent.getIntExtra("NOTE_ID", -1)
        if (currentIndex != -1 && currentIndex < notes.size) {
            loadNoteForEditing(notes[currentIndex])
        }



        binding.btnSave.setOnClickListener {
            if (currentIndex == -1) {
                addNote()
            } else {
                updateNote()
            }
        }

        binding.selectFileButton.setOnClickListener { openFilePicker() }
    }

    private fun openFilePicker() {
        filePickerLauncher.launch(arrayOf("*/*"))
    }

    private fun displayFilePreview(uri: Uri, fileType: String) {
        binding.etNoteContent.visibility = View.VISIBLE
        binding.etNoteContent.isEnabled = true
        binding.etNoteContent.isFocusableInTouchMode = true

        // Reset visibility for views
        binding.fileImageView.visibility = View.GONE
        binding.selectedFileName.visibility = View.VISIBLE

        if (fileType.startsWith("image/")) {
            binding.fileImageView.visibility = View.VISIBLE
            binding.fileImageView.setImageURI(uri)
        } else if (fileType == "application/pdf") {
            renderPdfPreview(uri)
        } else {
            // Unsupported file type
            binding.fileImageView.visibility = View.GONE
            binding.selectedFileName.text = getString(R.string.unsupported_file_type)
        }

        val documentFile = DocumentFile.fromSingleUri(this, uri)
        binding.selectedFileName.text = documentFile?.name ?: getString(R.string.file_selected)
    }



    private fun renderPdfPreview(uri: Uri) {
        try {
            val fileDescriptor = contentResolver.openFileDescriptor(uri, "r")
            val pdfRenderer = PdfRenderer(fileDescriptor!!)
            val page = pdfRenderer.openPage(0)

            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            binding.fileImageView.setImageBitmap(bitmap)
            binding.fileImageView.visibility = View.VISIBLE
            page.close()
            pdfRenderer.close()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to render PDF", Toast.LENGTH_SHORT).show()
        }
    }


    private fun loadNotes() {
        val file = File(filesDir, "notes.json")
        if (file.exists()) {
            val json = file.readText()
            try {
                val noteListType: Type = object : TypeToken<List<Note>>() {}.type
                val parsedNotes: List<Note> = Gson().fromJson(json, noteListType)
                notes = parsedNotes.toMutableList()
                Log.d("LoadNotes", "Successfully loaded ${notes.size} notes")
            } catch (e: Exception) {
                Log.e("LoadNotes", "Error parsing JSON: ${e.message}")
                notes = mutableListOf() // Initialize notes in case of error
            }
        }
    }


    private fun addNote() {
        val newTitle = binding.etNoteTitle.text.toString()
        val newContent = binding.etNoteContent.text.toString()

        val newNote = Note(
            title = newTitle,
            content = newContent,
            fileUri = selectedFileUri?.toString(),
            date = getCurrentDate()
        )

        notes.add(newNote) // Add to the global list
        saveNotesToFile() // Save updated notes
        setResult(RESULT_OK)
        finish()
    }

    private fun updateNote() {
        val updatedTitle = binding.etNoteTitle.text.toString()
        val updatedContent = binding.etNoteContent.text.toString()

        if (currentIndex in notes.indices) {
            notes[currentIndex] = notes[currentIndex].copy(
                title = updatedTitle,
                content = updatedContent,
                fileUri = selectedFileUri?.toString(), // Include fileUri here
                date = getCurrentDate()
            )
            saveNotesToFile() // Save updated notes
            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Error updating note", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadNoteForEditing(note: Note) {
        binding.etNoteTitle.setText(note.title)
        binding.etNoteContent.setText(note.content)
        binding.noteDate.text = note.date

        note.fileUri?.let {
            selectedFileUri = Uri.parse(it)
            val fileType = contentResolver.getType(selectedFileUri!!)
            if (fileType != null) {
                displayFilePreview(selectedFileUri!!, fileType)
            }
        }
    }

    private fun saveNotesToFile() {
        val json = Gson().toJson(notes)
        val file = File(filesDir, "notes.json")
        try {
            file.writeText(json)
            Log.d("SaveNotes", "Notes saved successfully: ${notes.size} notes")
        } catch (e: Exception) {
            Log.e("SaveNotes", "Error saving notes: ${e.message}")
        }
    }

    private fun loadQuestions() {
        try {
            val inputStream: InputStream = assets.open("journal_questions.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val type = object : TypeToken<QuestionsWrapper>() {}.type
            val questionsWrapper: QuestionsWrapper = Gson().fromJson(jsonString, type)
            questions = questionsWrapper.questions
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading questions: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun displayRandomQuestion() {
        if (questions.isNotEmpty()) {
            val randomQuestion = questions.random()
            binding.randomQuestionText.text = randomQuestion
            binding.randomQuestionText.visibility = View.VISIBLE
            binding.confirmQuestionButton.visibility = View.VISIBLE
        } else {
            Toast.makeText(this, "No questions available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmQuestion() {
        binding.etNoteTitle.setText(binding.randomQuestionText.text)
        binding.confirmQuestionButton.visibility = View.GONE
        binding.randomQuestionText.visibility = View.GONE
    }



    private fun getCurrentDate(): String {
        val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return dateFormatter.format(Date())
    }


    private fun loadNotesFromPrivateStorage(): String? {
        val file = File(filesDir, "notes.json")
        return if (file.exists()) {
            file.readText()
        } else null
    }

}
