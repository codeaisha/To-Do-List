package com.example.todolist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddTaskActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var buttonSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        // Initialize views
        editTextTitle = findViewById(R.id.editTextTitle)
        editTextDescription = findViewById(R.id.editTextDescription)
        buttonSave = findViewById(R.id.buttonSave)

        // ✍️ Pre-fill fields if editing
        val titleFromIntent = intent.getStringExtra("task_title")
        val descriptionFromIntent = intent.getStringExtra("task_description")

        if (!titleFromIntent.isNullOrEmpty()) {
            editTextTitle.setText(titleFromIntent)
        }

        if (!descriptionFromIntent.isNullOrEmpty()) {
            editTextDescription.setText(descriptionFromIntent)
        }

        // 💾 Save button click
        buttonSave.setOnClickListener {
            val title = editTextTitle.text.toString().trim()
            val description = editTextDescription.text.toString().trim()

            if (title.isNotEmpty() && description.isNotEmpty()) {
                val resultIntent = Intent().apply {
                    putExtra("task_title", title)
                    putExtra("task_description", description)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Please fill in both title and description", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
