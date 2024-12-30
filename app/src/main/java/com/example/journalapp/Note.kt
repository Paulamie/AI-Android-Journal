package com.example.journalapp

data class Note(
    val title: String,
    val content: String,
    val fileUri: String? = null, // Optional file URI
    var date: String = "", // Default empty string for compatibility
    var isSelected: Boolean = false, // Default selection state
    var emoji: String? = null // Emoji for mood
)

