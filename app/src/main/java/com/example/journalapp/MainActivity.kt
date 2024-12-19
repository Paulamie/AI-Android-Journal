package com.example.journalapp

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.journalapp.databinding.ActivityMainBinding
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import android.widget.RemoteViews
import java.io.InputStream
import java.util.Calendar
import com.google.gson.reflect.TypeToken


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
        .baseUrl("http://10.0.2.2:5000/") // Replace with your actual server URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

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
                startActivityForResult(intent, REQUEST_CODE_NOTE)
            }
        }, { position ->
            isInSelectionMode = true
            toggleSelection(position)
        })
        binding.notesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.notesRecyclerView.adapter = notesAdapter

        // Load notes and streak data
        reloadNotes()
        loadStreakData()
        updateStreakUI()

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

        binding.streakIcon.setOnClickListener {
            val intent = Intent(this, StreakActivity::class.java)
            startActivity(intent)
        }

        // Button to get advice based on notes
        binding.apiButton.setOnClickListener {
            getAdviceForNotes()
        }

        binding.refreshButton.setOnClickListener {
            getAdviceForNotes() // Re-fetch advice when refreshing
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_streak -> {
                // Launch the StreakActivity
                startActivity(Intent(this, StreakActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun reloadNotes() {
        val json = loadNotesFromPrivateStorage() ?: loadNotesFromAssets()
        if (json != null) {
            try {
                // Parse JSON as NotesResponse
                val response = Gson().fromJson(json, NotesResponse::class.java)
                notesList = response.notes.toMutableList()
                Log.d("ReloadNotes", "Loaded ${notesList.size} notes as NotesResponse")
            } catch (e: Exception) {
                Log.e("ReloadNotes", "Error parsing JSON as NotesResponse: ${e.message}")
                try {
                    // Fallback: Parse JSON as List<Note>
                    val noteListType = object : TypeToken<List<Note>>() {}.type
                    notesList = Gson().fromJson(json, noteListType)
                    Log.d("ReloadNotes", "Fallback: Loaded ${notesList.size} notes as List<Note>")
                } catch (e2: Exception) {
                    Log.e("ReloadNotes", "Error parsing JSON: ${e2.message}")
                    Toast.makeText(this, "Error loading notes", Toast.LENGTH_SHORT).show()
                }
            }

            notesAdapter.updateNotes(notesList) // Update adapter
        } else {
            Log.e("ReloadNotes", "Failed to load JSON from storage or assets")
            Toast.makeText(this, "Failed to load notes data", Toast.LENGTH_SHORT).show()
        }
    }



    private fun searchNotes(query: String?) {
        val filteredNotes = notesList.filter {
            it.title.contains(query ?: "", true) || it.content.contains(query ?: "", true)
        }
        notesAdapter.updateNotes(filteredNotes)
    }

    private fun toggleSelection(position: Int) {
        val note = notesAdapter.notes[position]
        note.isSelected = !note.isSelected

        if (note.isSelected) selectedNotes.add(note) else selectedNotes.remove(note)

        notesAdapter.notifyItemChanged(position)

        binding.deleteButton.visibility = if (selectedNotes.isEmpty()) View.GONE else View.VISIBLE
        isInSelectionMode = selectedNotes.isNotEmpty()
    }

    private fun deleteSelectedNotes() {
        notesAdapter.notes.removeAll(selectedNotes)
        notesAdapter.notifyDataSetChanged()
        saveNotesToFile(notesAdapter.notes)
        selectedNotes.clear()
        isInSelectionMode = false
        binding.deleteButton.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_NOTE && resultCode == RESULT_OK) {
            reloadNotes()
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

    private fun saveNotesToFile(notes: List<Note>) {
        val json = Gson().toJson(NotesResponse(notes)) // Save notes inside NotesResponse
        val file = File(filesDir, "notes.json")
        try {
            file.writeText(json)
            Log.d("SaveNotes", "JSON saved: $json")
        } catch (e: Exception) {
            Log.e("SaveNotes", "Error saving notes: ${e.message}")
            Toast.makeText(this, "Error saving notes", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAdviceForNotes() {
        val notes = notesAdapter.notes
        apiService.getAdvice(AdviceRequest(notes)).enqueue(object : Callback<AdviceResponse> {
            override fun onResponse(call: Call<AdviceResponse>, response: Response<AdviceResponse>) {
                if (response.isSuccessful) {
                    displayAdviceInRecyclerView(response.body()?.advice)
                    binding.apiButton.visibility = View.GONE
                    binding.refreshButton.visibility = View.VISIBLE
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

    private fun loadStreakData() {
        val prefs = getSharedPreferences("JournalAppPrefs", MODE_PRIVATE)
        streakCount = prefs.getInt("streak_count", 0)
        lastActiveDate = prefs.getLong("last_active_date", 0)
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
            appWidgetIds.forEach { appWidgetManager.updateAppWidget(it, views) }
        }
    }

    companion object {
        private const val REQUEST_CODE_NOTE = 1
    }
}
