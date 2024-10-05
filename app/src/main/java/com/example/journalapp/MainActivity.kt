package com.example.journalapp

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val plusButton: ImageButton = findViewById(R.id.plusButton)

        plusButton.setOnClickListener {
            Toast.makeText(this, "Plus button clicked!", Toast.LENGTH_SHORT).show()
        }
    }
}
