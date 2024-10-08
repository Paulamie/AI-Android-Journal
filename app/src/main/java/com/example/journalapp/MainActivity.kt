package com.example.journalapp

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val plusButton: ImageButton = findViewById(R.id.plusButton)

        plusButton.setOnClickListener {
            Toast.makeText(this, "Plus button clicked!", Toast.LENGTH_SHORT).show()
        }

        // Load the JSON from assets and parse it into notes
        val json = loadJSONFromAsset()
        if (json != null) {
            val notes = Gson().fromJson(json, NotesResponse::class.java).notes

            // Set up the RecyclerView with the parsed notes
            val notesRecyclerView: RecyclerView = findViewById(R.id.notesRecyclerView)
            notesRecyclerView.layoutManager = LinearLayoutManager(this)
            notesRecyclerView.adapter = NotesAdapter(notes)
        } else {
            Toast.makeText(this, "Failed to load notes data", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to load JSON from the assets folder
    private fun loadJSONFromAsset(): String? {
        return try {
            val inputStream: InputStream = assets.open("notes.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }
}
