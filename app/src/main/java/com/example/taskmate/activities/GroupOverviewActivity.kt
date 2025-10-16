package com.example.taskmate.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskmate.models.Email
import com.example.taskmate.models.User
import com.example.taskmate.models.UserId
import com.example.taskmate.repositories.IUserRepository
import com.example.taskmate.repositories.UserRepository
import com.example.taskmate.ui.theme.TaskMateTheme

@OptIn(ExperimentalMaterial3Api::class)
class GroupOverviewActivity : ComponentActivity() {
    private val viewModel: GroupOverviewViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                                    // You may want to move logout logic to the ViewModel for consistency
                                    UserRepository().logout()
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
                        GroupOverviewScreen(viewModel = viewModel)
                        BackHandler(enabled = true) { /* Do nothing to disable back */ }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupOverviewScreen(viewModel: GroupOverviewViewModel) {
    val viewState by viewModel.viewState.collectAsState()
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GreetingCard(viewState.userName)
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
            IconButton(onClick = {
                context.startActivity(Intent(context, CreateGroupActivity::class.java))
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add group"
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        if (viewState.isLoading) {
            CircularProgressIndicator()
        } else if (viewState.error != null) {
            Text(
                text = viewState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
            TextButton(onClick = { viewModel.clearError() }) {
                Text("Dismiss", color = MaterialTheme.colorScheme.error)
            }
        } else if (viewState.groups.isEmpty()) {
            Text(
                text = "No groups yet.",
                style = MaterialTheme.typography.headlineMedium
            )
        } else {
            // TODO: Render groups list
        }
    }
}

@Composable
fun GreetingCard(userName: String) {
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
}

@Preview(showBackground = true)
@Composable
fun GroupOverviewScreenPreview() {
    class FakeUserRepository : IUserRepository {
        override suspend fun getCurrentUser(): User? = User(UserId("1"), email = Email("preview@mail.dk"), name = "Preview User")
        override fun logout() {}
    }
    TaskMateTheme {
        GroupOverviewScreen(viewModel = GroupOverviewViewModel(FakeUserRepository()))
    }
}
