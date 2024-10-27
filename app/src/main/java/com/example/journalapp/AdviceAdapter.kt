package com.example.journalapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdviceAdapter(private val adviceList: List<String>) : RecyclerView.Adapter<AdviceAdapter.AdviceViewHolder>() {

    inner class AdviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val adviceTextView: TextView = view.findViewById(R.id.adviceText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_advice, parent, false)
        return AdviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdviceViewHolder, position: Int) {
        holder.adviceTextView.text = adviceList[position]
    }

    override fun getItemCount() = adviceList.size
}
