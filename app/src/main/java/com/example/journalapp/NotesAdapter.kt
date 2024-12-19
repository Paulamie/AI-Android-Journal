package com.example.journalapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotesAdapter(
    var notes: MutableList<Note>, // Mutable list for easy updates and deletion
    private val onNoteClicked: (Int) -> Unit,
    private val onNoteLongClicked: (Int) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.noteTitle)
        val content: TextView = itemView.findViewById(R.id.noteContent)
        val emoji: TextView = itemView.findViewById(R.id.noteEmoji)
        val checkBox: CheckBox = itemView.findViewById(R.id.noteCheckBox) // The checkbox
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }


    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.title.text = note.title

        // Show only the first line of content
        val firstLine = note.content.lines().firstOrNull() ?: ""
        holder.content.text = firstLine

        // Only fetch mood emoji if it's not already set
        if (holder.emoji.text.isEmpty() || holder.emoji.text == "❓") {
            val apiService = Retrofit.Builder()
                .baseUrl("http://10.0.2.2:5000/") // Replace with your server's URL
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)

            val request = AdviceRequest(listOf(note))
            apiService.getMood(request).enqueue(object : Callback<MoodResponse> {
                override fun onResponse(call: Call<MoodResponse>, response: Response<MoodResponse>) {
                    if (response.isSuccessful) {
                        val moodEmoji = response.body()?.mood ?: "❓"
                        holder.emoji.text = moodEmoji // Set the emoji
                        Log.d("NotesAdapter", "Mood for note '${note.title}': $moodEmoji")
                    } else {
                        holder.emoji.text = "❓" // Fallback emoji
                        Log.e("NotesAdapter", "Failed to get mood for note '${note.title}'")
                    }
                }

                override fun onFailure(call: Call<MoodResponse>, t: Throwable) {
                    holder.emoji.text = "❓" // Fallback emoji on error
                    Log.e("NotesAdapter", "Error fetching mood: ${t.message}")
                }
            })
        }

        // Set checkbox visibility and state
        holder.checkBox.visibility = if (note.isSelected) View.VISIBLE else View.GONE
        holder.checkBox.isChecked = note.isSelected

        // Set click listeners for item and checkbox
        holder.itemView.setOnClickListener {
            onNoteClicked(position)
        }

        holder.itemView.setOnLongClickListener {
            onNoteLongClicked(position)
            true
        }

        holder.checkBox.setOnClickListener {
            note.isSelected = !note.isSelected
            notifyItemChanged(position)
            Log.d("NotesAdapter", "Checkbox clicked for note '${note.title}', selected: ${note.isSelected}")
        }
    }


    override fun getItemCount(): Int = notes.size

    fun updateNotes(newNotes: List<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
        Log.d("NotesAdapter", "Adapter updated with ${newNotes.size} notes")
    }
}
