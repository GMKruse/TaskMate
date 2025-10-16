package com.example.taskmate.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.taskmate.ui.theme.TaskMateTheme
import com.example.taskmate.repositories.IUserRepository
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taskmate.repositories.UserRepository
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

@OptIn(ExperimentalMaterial3Api::class)
class GroupOverviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userRepository: IUserRepository = UserRepository(this)
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
                        contentAlignment = Alignment.TopCenter
                    ) {
                        GroupOverviewScreen(userRepository)
                        BackHandler(enabled = true) { /* Do nothing to disable back */ }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupOverviewScreen(userRepository: IUserRepository) {
    var userName by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val name = userRepository.getCurrentUserName()
        userName = name ?: "User"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Hello $userName",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Motivational quote of the day: Just do it!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your groups",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { /* TODO: Add group action */ }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add group"
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "No groups yet.",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GroupOverviewScreenPreview() {
    class FakeUserRepository : IUserRepository {
        override suspend fun getCurrentUserName(): String? = "Preview User"
        override fun logout() {}
    }
    TaskMateTheme {
        GroupOverviewScreen(userRepository = FakeUserRepository())
    }
}
