package com.example.journalapp

data class Note(
    val title: String,
    val content: String,
    var isSelected: Boolean = false
)
