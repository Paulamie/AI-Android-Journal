package com.example.journalapp

<<<<<<< HEAD
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
=======
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
>>>>>>> c86f773 (Reinitialize repository)
import com.example.journalapp.databinding.ActivityNoteDetailBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.InputStream

<<<<<<< HEAD

=======
>>>>>>> c86f773 (Reinitialize repository)
data class QuestionsWrapper(val questions: List<String>)

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailBinding
    private var notes: MutableList<Note> = mutableListOf()
<<<<<<< HEAD
    private var questions: List<String> = listOf()
    private var currentIndex = -1 // Default to -1 for new notes
=======
    private var questions: List<String> = emptyList()
    private var currentIndex = -1 // Default to -1 for new notes
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
>>>>>>> c86f773 (Reinitialize repository)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadNotes()
<<<<<<< HEAD
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
=======
        loadQuestions()

        binding.questionButton.setOnClickListener { displayRandomQuestion() }
        binding.confirmQuestionButton.setOnClickListener { confirmQuestion() }


        // Get the note ID from the intent to check if we're editing an existing note
>>>>>>> c86f773 (Reinitialize repository)
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
<<<<<<< HEAD
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
=======

        // Set up file picker button
        binding.selectFileButton.setOnClickListener { openFilePicker() }
    }

    // Method to open the file picker
    private fun openFilePicker() {
        filePickerLauncher.launch(arrayOf("*/*"))
    }

    private fun displayFilePreview(uri: Uri, fileType: String) {
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
                // Parse the saved file URI and store it in selectedFileUri
                selectedFileUri = Uri.parse(it)
                val fileType = contentResolver.getType(selectedFileUri!!)

                if (fileType != null) {
                    // Display file preview based on file type
                    displayFilePreview(selectedFileUri!!, fileType)
                } else {
                    // If file type can't be determined, show file name
                    binding.fileImageView.visibility = View.GONE
                    binding.selectedFileName.visibility = View.VISIBLE
                    val documentFile = DocumentFile.fromSingleUri(this, selectedFileUri!!)
                    val displayName = documentFile?.name ?: getString(R.string.file_selected)
                    binding.selectedFileName.text = displayName
                }
            } ?: run {
                // If no file URI is present, ensure UI elements are hidden
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
>>>>>>> c86f773 (Reinitialize repository)
        } else {
            Toast.makeText(this, "Error updating note", Toast.LENGTH_SHORT).show()
        }
    }

<<<<<<< HEAD
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
=======


    private fun saveNotesToFile() {
        val json = Gson().toJson(notes)
        val file = File(filesDir, "notes.json")
        file.writeText(json)
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
            val randomQuestion = questions.random() // Get a random question
            binding.randomQuestionText.text = randomQuestion // Display it in the TextView
            binding.randomQuestionText.visibility = View.VISIBLE // Make the TextView visible
            binding.confirmQuestionButton.visibility = View.VISIBLE // Make the Confirm button visible
        } else {
            Toast.makeText(this, "No questions available", Toast.LENGTH_SHORT).show()
        }
    }



    private fun confirmQuestion() {
        binding.etNoteTitle.setText(binding.randomQuestionText.text) // Set the question as the title
        binding.confirmQuestionButton.visibility = View.GONE // Hide the Confirm button after confirming
        binding.randomQuestionText.visibility = View.GONE // Optionally hide the random question
    }
}
>>>>>>> c86f773 (Reinitialize repository)
