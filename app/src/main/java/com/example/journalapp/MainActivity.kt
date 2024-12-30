package com.example.journalapp

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.journalapp.databinding.ActivityMainBinding
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.Calendar
import com.google.gson.reflect.TypeToken
import android.widget.RemoteViews


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isInSelectionMode = false
    private val selectedNotes = mutableListOf<Note>()
    private lateinit var notesAdapter: NotesAdapter
    private var notesList: MutableList<Note> = mutableListOf()



    // Streak information
    private var streakCount = 0
    private var lastActiveDate: Long = 0

    // Retrofit instance for API service
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:5001/") // Replace with your actual server URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    // ActivityResultLauncher
    private val noteActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                reloadNotes()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Set up RecyclerView and Adapter
        notesAdapter = NotesAdapter(notesList, { position ->
            if (isInSelectionMode) {
                toggleSelection(position)
            } else {
                val intent = Intent(this, NoteDetailActivity::class.java)
                intent.putExtra("NOTE_ID", position)
                noteActivityLauncher.launch(intent)
            }
        }, { position ->
            isInSelectionMode = true
            toggleSelection(position)
        })

        binding.notesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.notesRecyclerView.adapter = notesAdapter

        binding.AIbot.layoutManager = LinearLayoutManager(this)
        binding.AIbot.adapter = AdviceAdapter(emptyList())

        // Load notes and streak data
        reloadNotes()
        loadStreakData()
        updateStreakUI()

        // Floating action button for adding a new note
        binding.plusButton.setOnClickListener {
            if (!isInSelectionMode) {
                val intent = Intent(this, NoteDetailActivity::class.java)
                intent.putExtra("NOTE_ID", -1)
                noteActivityLauncher.launch(intent)
            }
        }

        // Delete button for deleting selected notes
        binding.deleteButton.setOnClickListener {
            deleteSelectedNotes()
        }

//        binding.streakIcon.setOnClickListener {
//            val intent = Intent(this, StreakActivity::class.java)
//            startActivity(intent)
//        }

        // Button to get advice based on notes
        binding.apiButton.setOnClickListener {
            getAdviceForNotes()
        }

        binding.refreshButton.setOnClickListener {
            getAdviceForNotes()
        }

//        binding.streakIcon.setOnClickListener {
//            val intent = Intent(this, StreakActivity::class.java)
//            startActivity(intent)
//        }


    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        // Access the SearchView from the menu
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView

        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    filterNotes(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filterNotes(newText)
                }
                return true
            }
        })

        return true
    }

    private fun loadStreakData() {
        val prefs = getSharedPreferences("JournalAppPrefs", MODE_PRIVATE)
        streakCount = prefs.getInt("streak_count", 0)
        lastActiveDate = prefs.getLong("last_active_date", 0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                true
            }
            R.id.streakIcon -> {
                val intent = Intent(this, StreakActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun filterNotes(query: String) {
        val filteredNotes = notesList.filter {
            it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
        }
        notesAdapter.updateNotes(filteredNotes)
    }


    private fun reloadNotes() {
        val json = loadNotesFromPrivateStorage() ?: loadNotesFromAssets()
        if (json != null) {
            try {
                val noteListType = object : TypeToken<List<Note>>() {}.type
                notesList = Gson().fromJson(json, noteListType)
                Log.d("ReloadNotes", "Loaded ${notesList.size} notes as List<Note>")
            } catch (e: Exception) {
                Log.e("ReloadNotes", "Error parsing JSON: ${e.message}")
                Toast.makeText(this, "Error loading notes", Toast.LENGTH_SHORT).show()
            }
            notesAdapter.updateNotes(notesList)
        } else {
            Log.e("ReloadNotes", "Failed to load JSON from storage or assets")
            Toast.makeText(this, "Failed to load notes data", Toast.LENGTH_SHORT).show()
        }
    }


    private fun toggleSelection(position: Int) {
        val notes = notesAdapter.getNotes()
        val note = notes[position]
        note.isSelected = !note.isSelected

        if (note.isSelected) {
            selectedNotes.add(note)
        } else {
            selectedNotes.remove(note)
        }

        notesAdapter.notifyItemChanged(position)

        // Update delete button visibility
        binding.deleteButton.visibility = if (selectedNotes.isEmpty()) View.GONE else View.VISIBLE

        // Reset selection mode if no notes are selected
        isInSelectionMode = selectedNotes.isNotEmpty()
        if (!isInSelectionMode) {
            binding.plusButton.isEnabled = true // Re-enable the add button
        } else {
            binding.plusButton.isEnabled = false // Disable add button while in selection mode
        }
    }



    private fun deleteSelectedNotes() {
        val notes = notesAdapter.getNotes().toMutableList() // Convert to mutable list
        notesList.removeAll(selectedNotes) // Correct
        notesAdapter.updateNotes(notes) // Update adapter
        saveNotesToFile() // Save updated notes to file
        selectedNotes.clear()
        isInSelectionMode = false

        // Reset button visibility and enable the add button
        binding.deleteButton.visibility = View.GONE
        binding.plusButton.isEnabled = true
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_NOTE && resultCode == RESULT_OK) {
            reloadNotes() // Refresh the notes from storage
            notesAdapter.notifyDataSetChanged() // Notify the adapter
        }
    }



    private fun loadNotesFromPrivateStorage(): String? {
        val file = File(filesDir, "notes.json")
        return if (file.exists()) {
            try {
                file.readText()
            } catch (e: Exception) {
                Log.e("LoadNotes", "Error reading file: ${e.message}")
                null
            }
        } else null
    }



    private fun loadNotesFromAssets(): String? {
        return try {
            assets.open("notes.json").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            null
        }
    }

    private fun saveNotesToFile() {
        val json = Gson().toJson(notesList) // Convert notesList to JSON
        val file = File(filesDir, "notes.json")
        try {
            file.writeText(json)
            Log.d("SaveNotes", "Notes saved successfully to: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("SaveNotes", "Error saving notes: ${e.message}")
        }
    }

    private fun getAdviceForNotes() {
        apiService.getAdvice(AdviceRequest(notesList)).enqueue(object : Callback<AdviceResponse> {
            override fun onResponse(call: Call<AdviceResponse>, response: Response<AdviceResponse>) {
                if (response.isSuccessful) {
                    displayAdviceInRecyclerView(response.body()?.advice)
                    binding.apiButton.visibility = View.GONE
                    binding.refreshButton.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this@MainActivity, "Failed to fetch advice", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AdviceResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayAdviceInRecyclerView(advice: String?) {
        val adviceList = listOf(advice ?: "No advice available")
        binding.AIbot.layoutManager = LinearLayoutManager(this)
        binding.AIbot.adapter = AdviceAdapter(adviceList)
    }



    private fun updateStreakUI() {
        val currentDate = Calendar.getInstance().apply {
            // Set the time to midnight to only compare dates
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Check if the current date is different from the last active date
        if (currentDate > lastActiveDate) {
            streakCount++ // Increment streak only if a new day has passed

            // Update the last active date to today's date
            val prefs = getSharedPreferences("JournalAppPrefs", MODE_PRIVATE)
            prefs.edit()
                .putInt("streak_count", streakCount)
                .putLong("last_active_date", currentDate)
                .apply()

            updateWidget() // Update the widget with the new streak count
        }
    }

    private fun updateWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, JournalAppWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isNotEmpty()) {
            val views = RemoteViews(packageName, R.layout.widget)
            views.setTextViewText(R.id.widget_streak_count, "Current Streak: $streakCount")

            // Update all widgets
            appWidgetIds.forEach { appWidgetManager.updateAppWidget(it, views) }
        }
    }



    companion object {
        private const val REQUEST_CODE_NOTE = 1
    }
}
