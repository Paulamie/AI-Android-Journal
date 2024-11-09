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
    var notes: MutableList<Note>, // Make the list mutable for deletion and updating
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
        holder.content.text = note.content

        // Make an API call to get the mood emoji for this note
        val apiService = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5000/") // Ensure this matches your server's URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        val request = AdviceRequest(listOf(note))
        apiService.getMood(request).enqueue(object : Callback<MoodResponse> {
            override fun onResponse(call: Call<MoodResponse>, response: Response<MoodResponse>) {
                if (response.isSuccessful) {
                    val moodEmoji = response.body()?.mood ?: ""
                    holder.emoji.text = moodEmoji // Set the emoji on the note
                } else {
                    holder.emoji.text = "❓" // Fallback emoji in case of failure
                }
            }

            override fun onFailure(call: Call<MoodResponse>, t: Throwable) {
                holder.emoji.text = "❓" // Fallback emoji on error
            }
        })

        // Show or hide the checkbox based on selection mode
        holder.checkBox.visibility = if (note.isSelected) View.VISIBLE else View.GONE
        holder.checkBox.isChecked = note.isSelected

        // Normal note click behavior
        holder.itemView.setOnClickListener {
            onNoteClicked(position)
        }

        // Handle long click to initiate selection
        holder.itemView.setOnLongClickListener {
            onNoteLongClicked(position)
            true
        }

        // Handle checkbox click to toggle selection
        holder.checkBox.setOnClickListener {
            note.isSelected = !note.isSelected
            notifyItemChanged(position)
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
