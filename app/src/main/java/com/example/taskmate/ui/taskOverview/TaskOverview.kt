package com.example.taskmate.ui.taskOverview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    var isDialogOpen by remember { mutableStateOf(false) }
    var creating by remember { mutableStateOf(false) }

    DisposableEffect(groupId) {
        val unsubscribe = taskRepository.listenToTasksForGroup(groupId) { updated ->
            tasks = updated
        }
        onDispose {
            unsubscribe()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { isDialogOpen = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add task")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(12.dp)
        ) {
            Text(
                text = "Tasks for group",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tasks yet. Use the + button to add one.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(tasks) { task ->
                        TaskListItem(
                            task = task,
                            onClick = { onTaskClick(task) }
                        )
                        Divider()
                    }
                }
            }
        }

        if (isDialogOpen) {
            var name by remember { mutableStateOf("") }
            var description by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = {
                    if (!creating) {
                        isDialogOpen = false
                    }
                },
                title = { Text("Create Task") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Task name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (name.isBlank()) return@TextButton
                            creating = true
                            val newTask = Task(
                                id = "",
                                name = name.trim(),
                                description = description.trim(),
                                isCompleted = false,
                                groupId = groupId
                            )
                            taskRepository.createTask(newTask) { success, _ ->
                                creating = false
                                if (success) {
                                    isDialogOpen = false
                                }
                            }
                        },
                        enabled = !creating
                    ) {
                        Text(if (creating) "Creating..." else "Create")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            if (!creating) isDialogOpen = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.name, style = MaterialTheme.typography.titleSmall)
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = null
            )
        }
    }
}
