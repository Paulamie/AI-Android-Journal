package com.example.journalapp

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
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
        if (currentIndex != -1) {
            loadNoteForEditing(currentIndex)
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
            // Display image
            binding.fileImageView.visibility = View.VISIBLE
            binding.fileImageView.setImageURI(uri)
        } else if (fileType == "application/pdf") {
            // Render PDF preview
            renderPdfPreview(uri)
        }

        // Show file name
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

    private fun loadNoteForEditing(noteId: Int) {
        if (noteId in notes.indices) {
            val note = notes[noteId]
            binding.etNoteTitle.setText(note.title)
            binding.etNoteContent.setText(note.content)

            note.fileUri?.let {
                selectedFileUri = Uri.parse(it)
                val fileType = contentResolver.getType(selectedFileUri!!)
                Log.d("NoteDetailActivity", "File URI: $it")
                Log.d("NoteDetailActivity", "File Type: $fileType")

                if (fileType != null) {
                    displayFilePreview(selectedFileUri!!, fileType)
                } else {
                    Log.e("NoteDetailActivity", "Unsupported file type for URI: $it")
                    binding.fileImageView.visibility = View.GONE
                    binding.selectedFileName.visibility = View.VISIBLE
                    binding.selectedFileName.text = getString(R.string.file_selected)
                }
            } ?: run {
                Log.w("NoteDetailActivity", "No file URI found for note.")
                binding.fileImageView.visibility = View.GONE
                binding.selectedFileName.visibility = View.GONE
            }
        }
    }

    private fun addNote() {
        val newTitle = binding.etNoteTitle.text.toString()
        val newContent = binding.etNoteContent.text.toString()
        val noteFileUri = selectedFileUri?.toString() // Save the file URI

        notes.add(Note(newTitle, newContent, noteFileUri)) // Add new note to the list
        saveNotesToFile() // Save all notes to a JSON file
        setResult(RESULT_OK)
        finish()
    }


    private fun updateNote() {
        val updatedTitle = binding.etNoteTitle.text.toString()
        val updatedContent = binding.etNoteContent.text.toString()
        val noteFileUri = selectedFileUri?.toString() // Save the updated file URI

        if (currentIndex in notes.indices) {
            notes[currentIndex] = notes[currentIndex].copy(
                title = updatedTitle,
                content = updatedContent,
                fileUri = noteFileUri // Update the file URI
            )
            saveNotesToFile() // Save all notes to a JSON file
            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Error updating note", Toast.LENGTH_SHORT).show()
        }
    }


    private fun saveNotesToFile() {
        val json = Gson().toJson(notes)
        val file = File(filesDir, "notes.json")
        file.writeText(json)
        Log.d("SaveNotes", "Notes saved to: ${file.absolutePath}")
    }


    private fun loadNotes() {
        val file = File(filesDir, "notes.json")
        if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<List<Note>>() {}.type
            notes = Gson().fromJson(json, type)
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
}
