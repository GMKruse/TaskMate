package com.example.taskmate.ui.groupOverview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskmate.models.Group
import com.example.taskmate.models.DataState

@Composable
fun GroupOverviewScreen(
    viewModel: GroupOverviewViewModel,
    onNavigateToCreateGroup: () -> Unit = {},
    onNavigateToGroupDetails: (String) -> Unit = {}
) {
    val viewState by viewModel.viewState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GreetingCard(viewState.user.name, viewState.quote)

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
            IconButton(onClick = onNavigateToCreateGroup) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add group"
                )
            }
        }

        when (val groupsState = viewState.groups) {
            is DataState.Loading -> {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            is DataState.Error -> {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            is DataState.Data -> {
                val groups = groupsState.data

                if (groups.isEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "No groups yet.",
                        style = MaterialTheme.typography.headlineMedium
                    )
                } else {
                    GroupList(
                        groups = groups,
                        onGroupClick = onNavigateToGroupDetails
                    )
                }
            }
        }
    }
}

@Composable
fun GreetingCard(userName: String, quoteState: DataState<String, Nothing?>) {
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
            when (quoteState) {
                is DataState.Loading -> {
                    Text(
                        text = "Loading quote...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                is DataState.Data -> {
                    Text(
                        text = "Motivational quote of the day: ${quoteState.data}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                is DataState.Error -> {
                    Text(
                        text = "Motivational quote of the day: Just do it!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun GroupList(
    groups: List<Group>,
    onGroupClick: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(items = groups) { group ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 0.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                onClick = {
                    onGroupClick(group.id)
                }
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
