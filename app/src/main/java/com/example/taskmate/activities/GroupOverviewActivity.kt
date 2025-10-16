package com.example.taskmate.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.taskmate.ui.theme.TaskMateTheme
import com.example.taskmate.repositories.UserRepository

@OptIn(ExperimentalMaterial3Api::class)
class GroupOverviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userRepository = UserRepository(this)
        setContent {
            TaskMateTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("Groups") },
                            actions = {
                                val context = this@GroupOverviewActivity
                                TextButton(onClick = {
                                    userRepository.logout()
                                    val intent = Intent(context, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                }) {
                                    Text("Log out")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        GroupOverviewScreen()
                        BackHandler(enabled = true) { /* Do nothing to disable back */ }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupOverviewScreen() {
    Text(
        text = "No groups yet.",
        style = MaterialTheme.typography.headlineMedium
    )
}

@Preview(showBackground = true)
@Composable
fun GroupOverviewScreenPreview() {
    TaskMateTheme {
        GroupOverviewScreen()
    }
}
