package com.example.journalapp

data class Note(
    val title: String,
    val content: String,
    val fileUri: String? = null, // Optional file URI
    var isSelected: Boolean = false // Default selection state
)
