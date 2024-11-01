package com.example.journalapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
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
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isInSelectionMode = false
    private val selectedNotes = mutableListOf<Note>()
    private lateinit var notesAdapter: NotesAdapter
    private var notesList: MutableList<Note> = mutableListOf()

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

        // Set refreshButton initially to GONE
        binding.refreshButton.visibility = View.GONE
        Log.d("MainActivity", "Refresh button initially set to GONE")



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

        reloadNotes()

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

        binding.refreshButton.setOnClickListener {
            getAdviceForNotes() // Re-fetch advice when refreshing
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search notes..."

        // Set up lambda-based expand/collapse actions for the search view
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                binding.apiButton.visibility = View.GONE
                binding.refreshButton.visibility = View.GONE
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // Only show apiButton if refreshButton is not visible
                if (binding.refreshButton.visibility != View.VISIBLE) {
                    binding.apiButton.visibility = View.VISIBLE
                }
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchNotes(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchNotes(newText)
                return true
            }
        })

        return true
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
                R.id.action_logout -> {
                    Toast.makeText(this, "Logout selected", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun reloadNotes() {
        val json = loadNotesFromPrivateStorage() ?: loadNotesFromAssets()
        if (json != null) {
            notesList = Gson().fromJson(json, NotesResponse::class.java).notes.toMutableList()
            notesAdapter.updateNotes(notesList)
            Log.d("MainActivity", "Notes loaded: ${notesList.size}")
        } else {
            Toast.makeText(this, "Failed to load notes data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchNotes(query: String?) {
        Log.d("MainActivity", "Search query: $query")
        val filteredNotes = if (!query.isNullOrEmpty()) {
            notesList.filter { note ->
                val matches = note.title.contains(query, ignoreCase = true) || note.content.contains(query, ignoreCase = true)
                Log.d("MainActivity", "Note '${note.title}' match: $matches")
                matches
            }
        } else {
            notesList
        }

        Log.d("MainActivity", "Filtered notes count: ${filteredNotes.size}")
        notesAdapter.updateNotes(filteredNotes)
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

        selectedNotes.clear()
        isInSelectionMode = false
        binding.deleteButton.visibility = View.GONE

        saveNotesToFile(adapter.notes)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_NOTE && resultCode == RESULT_OK) {
            reloadNotes()
        }
    }

    private fun loadNotesFromPrivateStorage(): String? {
        val file = File(filesDir, "notes.json")
        return if (file.exists()) file.readText() else null
    }

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


    private fun getAdviceForNotes() {
        val notes = (binding.notesRecyclerView.adapter as? NotesAdapter)?.notes ?: return
        val request = AdviceRequest(notes)

        apiService.getAdvice(request).enqueue(object : Callback<AdviceResponse> {
            override fun onResponse(call: Call<AdviceResponse>, response: Response<AdviceResponse>) {
                if (response.isSuccessful) {
                    val advice = response.body()?.advice
                    displayAdviceInRecyclerView(advice)

                    // Hide "Get Advice" and show "Refresh" button
                    binding.apiButton.visibility = View.GONE
                    binding.refreshButton.visibility = View.VISIBLE
                    Log.d("MainActivity", "Visibility changed: apiButton=GONE, refreshButton=VISIBLE")
                } else {
                    Toast.makeText(this@MainActivity, "Failed to get advice", Toast.LENGTH_SHORT).show()
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

    companion object {
        private const val REQUEST_CODE_NOTE = 1
    }
}



//    private fun checkAndUpdateStreak() {
//        val currentTime = System.currentTimeMillis()
//        val calendar = Calendar.getInstance()
//        calendar.timeInMillis = currentTime
//        val today = calendar.get(Calendar.DAY_OF_YEAR)
//        val lastDate = Calendar.getInstance().apply { timeInMillis = streak.lastDate }
//
//        if (today == lastDate.get(Calendar.DAY_OF_YEAR) && streak.lastDate != 0L) {
//            return
//        } else if (today == lastDate.get(Calendar.DAY_OF_YEAR) + 1) {
//            streak.count++
//        } else {
//            streak.count = 1
//        }
//
//        streak.lastDate = currentTime
//
//        val intent = Intent(this, StreakActivity::class.java).apply {
//            putExtra("STREAK_DATA", streak)
//        }
//        startActivity(intent)
//    }
//}

