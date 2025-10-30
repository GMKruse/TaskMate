package com.example.taskmate.ui.taskOverview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskmate.models.Task
import com.example.taskmate.repositories.ITaskRepository
import com.example.taskmate.repositories.TaskRepository

@Composable
fun TaskOverview(
    groupId: String,
    taskRepository: ITaskRepository = TaskRepository(),
    onTaskClick: (Task) -> Unit = {}
) {
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // Hent tasks ved første composition eller når groupId ændres
    LaunchedEffect(groupId) {
        loading = true
        taskRepository.fetchTasksForGroup(groupId) { fetched ->
            tasks = fetched
            loading = false
        }
    }

    Column(modifier = Modifier.padding(12.dp)) {

        // Add Test Task button
        Button(onClick = {
            val newTask = Task(
                id = "", // Firestore genererer ID
                name = "Test Task ${tasks.size + 1}",
                description = "Auto-generated test",
                isCompleted = false,
                groupId = groupId
            )

            taskRepository.createTask(newTask) { success, _ ->
                if (success) {
                    taskRepository.fetchTasksForGroup(groupId) { updated ->
                        tasks = updated
                    }
                }
            }
        }) {
            Text("Add Test Task")
        }

        Text(
            text = "Tasks for group",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
        )

        if (loading) {
            Text(text = "Loading...", modifier = Modifier.padding(8.dp))
        }

        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(tasks) { task ->
                TaskListItem(
                    task = task,
                    onClick = { onTaskClick(task) }
                )
                Divider() // Changed to Divider (not deprecated)
            }
        }
    }
}

@Composable
fun TaskListItem(
    task: Task,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = task.name, style = MaterialTheme.typography.titleSmall)
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = null
            )
        }
    }
}
