package com.example.journalapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.journalapp.databinding.ActivityMainBinding
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.InputStream
import android.widget.Button
import android.widget.TextView
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isInSelectionMode = false
    private val selectedNotes = mutableListOf<Note>() // List of selected notes for deletion

    // Streak information
    private var streakCount = 0
    private var lastActiveDate: Long = 0

    // Retrofit instance for API service
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:5000/") // Replace with your actual server URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        reloadNotes()
        updateStreakUI() //streak page update

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

        // Show dropdown menu when menuButton is clicked
        binding.menuButton.setOnClickListener { view ->
            showDropdownMenu(view)
        }

        // Button to get advice based on notes
        binding.apiButton.setOnClickListener {
            getAdviceForNotes()
        }
    }

    private fun showDropdownMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        val inflater: MenuInflater = popupMenu.menuInflater
        inflater.inflate(R.menu.menu_main, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_help -> {
                    Toast.makeText(this, "Help selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_streak -> { // Handle the "Streak" option
                    val intent = Intent(this, StreakActivity::class.java) // Replace with your actual Streak Activity
                    startActivity(intent)
                    true
                }
                R.id.action_logout -> {
                    Toast.makeText(this, "Logout selected", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
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

    // Fetch advice for the selected notes and display in RecyclerView
    private fun getAdviceForNotes() {
        val notes = (binding.notesRecyclerView.adapter as? NotesAdapter)?.notes ?: return
        val request = AdviceRequest(notes)

        apiService.getAdvice(request).enqueue(object : Callback<AdviceResponse> {
            override fun onResponse(call: Call<AdviceResponse>, response: Response<AdviceResponse>) {
                if (response.isSuccessful) {
                    val advice = response.body()?.advice
                    displayAdviceInRecyclerView(advice)
                } else {
                    Toast.makeText(this@MainActivity, "Failed to get advice", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AdviceResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Display advice in AIbot RecyclerView
    private fun displayAdviceInRecyclerView(advice: String?) {
        val adviceList = listOf(advice ?: "No advice available")
        binding.AIbot.layoutManager = LinearLayoutManager(this)
        binding.AIbot.adapter = AdviceAdapter(adviceList)
    }

    // Update the streak count based on activity
    private fun updateStreak() {
        val currentDate = Calendar.getInstance().timeInMillis
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

        // Check if last active date is set
        if (lastActiveDate != 0L) {
            val lastActiveDay = Calendar.getInstance().apply { timeInMillis = lastActiveDate }.get(Calendar.DAY_OF_YEAR)

            if (today == lastActiveDay) {
                // Already active today, do nothing
                return
            } else if (today == lastActiveDay + 1) {
                // Active day after last active day
                streakCount++
            } else {
                // Streak broken, reset count
                streakCount = 1
            }
        } else {
            // First activity
            streakCount = 1
        }

        lastActiveDate = currentDate
        saveStreakData()
        updateStreakUI()
    }

    // Save streak data to shared preferences or file
    private fun saveStreakData() {
        val prefs = getSharedPreferences("JournalAppPrefs", MODE_PRIVATE)
        prefs.edit()
            .putInt("streak_count", streakCount)
            .putLong("last_active_date", lastActiveDate)
            .apply()
    }

    // Update streak UI elements
    private fun updateStreakUI() {
            val prefs = getSharedPreferences("JournalAppPrefs", MODE_PRIVATE)
            val editor = prefs.edit()
            streakCount++

            // Save the updated streak data
            editor.putInt("streak_count", streakCount)
            editor.putLong("last_active_date", System.currentTimeMillis()) // Update last active date
            editor.apply()
        }

    companion object {
        private const val REQUEST_CODE_NOTE = 1
    }
}

