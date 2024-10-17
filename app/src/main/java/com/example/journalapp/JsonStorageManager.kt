//package com.example.journalapp
//
//import android.content.Context
//import kotlinx.serialization.decodeFromString
//import kotlinx.serialization.encodeToString
//import kotlinx.serialization.json.Json
//import java.io.File
//
//class JsonStorageManager(private val context: Context) {
//    private val fileName = "notes.json"
//    private val jsonFormat = Json { ignoreUnknownKeys = true }
//
//    fun saveNotes(notes: List<Note>) {
//        try {
//            val jsonString = jsonFormat.encodeToString(notes)
//            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
//                it.write(jsonString.toByteArray())
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            // Consider logging this exception or notifying the user if necessary
//        }
//    }
//
//    fun loadNotes(): List<Note> {
//        try {
//            val file = File(context.filesDir, fileName)
//            if (!file.exists()) return emptyList()
//
//            val jsonString = context.openFileInput(fileName).bufferedReader().use { it.readText() }
//            return jsonFormat.decodeFromString(jsonString)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            // Handle the error accordingly, possibly logging or informing the user
//            return emptyList() // Return an empty list if there's an error
//        }
//    }
//}
