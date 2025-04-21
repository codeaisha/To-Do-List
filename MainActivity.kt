package com.example.todolist

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val ADD_TASK_REQUEST = 1
    private val EDIT_TASK_REQUEST = 2

    private lateinit var prefs: SharedPreferences
    private val taskList = mutableListOf<Task>()
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var recyclerView: RecyclerView

    private val PERMISSION_REQUEST_CODE = 101
    private var editingPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize SharedPreferences
        prefs = getSharedPreferences("tasks_prefs", MODE_PRIVATE)
        loadTasks() // ← Populate taskList from prefs

        // Ask Notification Permission if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE
            )
        }

        // RecyclerView + Adapter setup
        recyclerView = findViewById(R.id.recyclerViewTasks)
        taskAdapter = TaskAdapter(taskList) { position ->
            // Edit on click
            editingPosition = position
            val task = taskList[position]
            Intent(this, AddTaskActivity::class.java).apply {
                putExtra("task_title", task.title)
                putExtra("task_description", task.description)
            }.also {
                startActivityForResult(it, EDIT_TASK_REQUEST)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = taskAdapter

        // Add new task
        findViewById<FloatingActionButton>(R.id.fabAddTask).setOnClickListener {
            editingPosition = -1
            startActivityForResult(
                Intent(this, AddTaskActivity::class.java),
                ADD_TASK_REQUEST
            )
        }

        createNotificationChannel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        val title = data?.getStringExtra("task_title") ?: return
        val description = data.getStringExtra("task_description") ?: return
        if (title.isBlank() || description.isBlank()) return

        // Build timestamp
        val parts = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            .format(Date()).split(" ")
        val date = parts[0]
        val time = "${parts[1]} ${parts[2]}"
        val task = Task(title, description, date, time)

        if (requestCode == ADD_TASK_REQUEST) {
            // Add
            taskList.add(task)
            taskAdapter.notifyItemInserted(taskList.lastIndex)
            recyclerView.scrollToPosition(taskList.lastIndex)
            Toast.makeText(this, "Task Added: $title", Toast.LENGTH_SHORT).show()
            showNotification(title, date, time)

        } else if (requestCode == EDIT_TASK_REQUEST && editingPosition != -1) {
            // Edit
            taskList[editingPosition] = task
            taskAdapter.notifyItemChanged(editingPosition)
            Toast.makeText(this, "Task Updated", Toast.LENGTH_SHORT).show()
            editingPosition = -1
        }

        saveTasks() // ← Persist changes
    }

    override fun onPause() {
        super.onPause()
        saveTasks() // ← Also save on background
    }

    private fun saveTasks() {
        val array = JSONArray()
        taskList.forEach { t ->
            val obj = JSONObject()
            obj.put("title", t.title)
            obj.put("description", t.description)
            obj.put("date", t.date)
            obj.put("time", t.time)
            array.put(obj)
        }
        prefs.edit()
            .putString("tasks", array.toString())
            .apply()
    }

    private fun loadTasks() {
        val json = prefs.getString("tasks", null) ?: return
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            taskList.add(
                Task(
                    obj.getString("title"),
                    obj.getString("description"),
                    obj.getString("date"),
                    obj.getString("time")
                )
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "task_channel",
                "Task Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for task addition notifications"
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, date: String, time: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            return
        }
        val notif = NotificationCompat.Builder(this, "task_channel")
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("New Task Added")
            .setContentText("$title\nDate: $date\nTime: $time")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notif)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, perms: Array<String>, results: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, perms, results)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val granted = results.isNotEmpty() && results[0] == PackageManager.PERMISSION_GRANTED
            Toast.makeText(
                this,
                if (granted) "Notification permission granted" else "Notification denied",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
