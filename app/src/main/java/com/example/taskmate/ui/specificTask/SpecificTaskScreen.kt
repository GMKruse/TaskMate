package com.example.taskmate.ui.specificTask

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskmate.models.DataState

@Composable
fun SpecificTaskScreen(
    viewModel: SpecificTaskViewModel,
    onBack: () -> Unit = {}
) {
    val viewState by viewModel.viewState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val taskState = viewState.task) {
            is DataState.Loading -> {
                CircularProgressIndicator()
            }
            is DataState.Error -> {
                Text(text = taskState.error, color = MaterialTheme.colorScheme.error)
                Button(onClick = onBack) { Text("Back") }
            }
            is DataState.Data -> {
                val task = taskState.data
                Text(text = task.name, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = task.description)
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { checked ->
                            viewModel.setTaskCompleted(checked)
                        }
                    )
                    Text("Completed")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.deleteTask { success ->
                                if (success) onBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.onError)
                    }
                }
            }
        }
    }
}