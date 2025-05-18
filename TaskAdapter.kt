package com.example.todolist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val taskList: MutableList<Task>,
    private val onEditClick: (Int) -> Unit // callback for editing
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.textViewTitle)
        val description: TextView = itemView.findViewById(R.id.textViewDescription)
        val date: TextView = itemView.findViewById(R.id.textViewDate)
        val time: TextView = itemView.findViewById(R.id.textViewTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]

        holder.title.text = task.title
        holder.description.text = task.description
        holder.date.text = task.date
        holder.time.text = task.time

        // EDIT: On Click
        holder.itemView.setOnClickListener {
            onEditClick(position)
        }

        // DELETE: On Long Click
        holder.itemView.setOnLongClickListener {
            taskList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, taskList.size)
            Toast.makeText(holder.itemView.context, "Task Deleted", Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun getItemCount(): Int = taskList.size

    fun getTaskAt(position: Int): Task {
        return taskList[position]
    }

    fun updateTask(position: Int, updatedTask: Task) {
        taskList[position] = updatedTask
        notifyItemChanged(position)
    }

    fun updateTasks(newTasks: List<Task>) {
        taskList.clear()
        taskList.addAll(newTasks)
        notifyDataSetChanged()
    }
}
