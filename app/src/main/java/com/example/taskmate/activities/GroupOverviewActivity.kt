package com.example.taskmate.activities

import GroupRepository
import IGroupRepository
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
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
import com.example.taskmate.models.Group
import com.example.taskmate.models.User
import com.example.taskmate.models.UserId
import com.example.taskmate.repositories.IUserRepository
import com.example.taskmate.repositories.UserRepository
import com.example.taskmate.ui.theme.TaskMateTheme

@OptIn(ExperimentalMaterial3Api::class)
class GroupOverviewActivity : ComponentActivity() {
    private val viewModel: GroupOverviewViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val userRepository = UserRepository()
                val groupRepository: IGroupRepository = GroupRepository()
                @Suppress("UNCHECKED_CAST")
                return GroupOverviewViewModel(userRepository, groupRepository) as T
            }
        }
    }
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
fun GroupOverviewScreen(
    viewModel: GroupOverviewViewModel
) {
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
        if (viewState.isLoading) {
            CircularProgressIndicator()
        } else if (viewState.error != null) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = viewState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        } else if (viewState.groups.isEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "No groups yet.",
                style = MaterialTheme.typography.headlineMedium
            )
        } else {
            GroupList(groups = viewState.groups)
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

@Composable
fun GroupList(groups: List<com.example.taskmate.models.Group>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(items =groups) { group ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 0.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Go to group"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GroupOverviewScreenEmptyPreview() {
    class FakeUserRepository : IUserRepository {
        override suspend fun getCurrentUser() = User(UserId("1"), email = Email("preview@mail.dk"), name = "Preview User")
        override fun logout() {}
    }
    class FakeGroupRepository : IGroupRepository {
        override fun createGroup(group: com.example.taskmate.models.Group, onComplete: (Boolean, String?) -> Unit) {}
        override fun fetchGroupsForUser(userId: String, onResult: (List<com.example.taskmate.models.Group>) -> Unit) {
            onResult(emptyList())
        }
    }
    TaskMateTheme {
        GroupOverviewScreen(
            viewModel = GroupOverviewViewModel(FakeUserRepository(), FakeGroupRepository())
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GroupOverviewScreenWithGroupsPreview() {
    class FakeUserRepository : IUserRepository {
        override suspend fun getCurrentUser() =
            User(UserId("1"), email = Email("preview@mail.dk"), name = "Preview User")

        override fun logout() {}
    }

    class FakeGroupRepository : IGroupRepository {
        override fun createGroup(group: Group, onComplete: (Boolean, String?) -> Unit) {}
        override fun fetchGroupsForUser(userId: String, onResult: (List<Group>) -> Unit) {
            onResult(
                listOf(
                    Group(
                        id = "g1",
                        name = "Preview Group 1",
                        createdBy = UserId("1"),
                        members = listOf(Email("preview@mail.dk")),
                        createdAt = System.currentTimeMillis()
                    ),
                    Group(
                        id = "g2",
                        name = "Preview Group 2",
                        createdBy = UserId("1"),
                        members = listOf(Email("preview@mail.dk")),
                        createdAt = System.currentTimeMillis()
                    )
                )
            )
        }
    }
    TaskMateTheme {
        GroupOverviewScreen(
            viewModel = GroupOverviewViewModel(FakeUserRepository(), FakeGroupRepository())
        )
    }
}