package com.example.journalapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotesAdapter(
    val notes: MutableList<Note>, // Make the list mutable for deletion
    private val onNoteClicked: (Int) -> Unit,
    private val onNoteLongClicked: (Int) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.noteTitle)
        val content: TextView = itemView.findViewById(R.id.noteContent)
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
}

