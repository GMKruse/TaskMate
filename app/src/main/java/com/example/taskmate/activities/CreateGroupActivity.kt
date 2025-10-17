package com.example.taskmate.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskmate.models.Email
import com.example.taskmate.ui.theme.TaskMateTheme
import com.example.taskmate.models.ViewState

@OptIn(ExperimentalMaterial3Api::class)
class CreateGroupActivity : ComponentActivity() {
    private val viewModel: CreateGroupViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskMateTheme {
                CreateGroupScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(viewModel: CreateGroupViewModel, onBack: () -> Unit) {
    val viewState by viewModel.viewState.collectAsState()
    val groupName by viewModel.groupName.collectAsState()
    val emailInput by viewModel.emailInput.collectAsState()
    val memberEmails by viewModel.memberEmails.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val creating by viewModel.creatingGroup.collectAsState()
    val focusManager = LocalFocusManager.current

    // derive values from sealed viewState
    val currentUserEmail: Email? = when (val s = viewState) {
        is ViewState.Data -> s.data.currentUser.email
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New group") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        currentUserEmail?.let { viewModel.createGroup(it) { success -> if (success) onBack() } }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !creating && groupName.isNotBlank() && currentUserEmail != null
                ) {
                    if (creating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Create group", fontSize = 18.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Operation-level loading bar or error banner
            when (viewState) {
                is ViewState.Loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                }
                is ViewState.Error -> {
                    val msg = (viewState as ViewState.Error).error
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = msg, color = MaterialTheme.colorScheme.onError, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { /* TODO: FIX THIS */ }) {
                                Text(text = "Dismiss", color = MaterialTheme.colorScheme.onError)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                else -> { /* no op */ }
            }

            OutlinedTextField(
                value = groupName,
                onValueChange = { if (!creating) viewModel.onGroupNameChange(it) },
                label = { Text("Group name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !creating,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (memberEmails.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 120.dp)
                ) {
                    items(memberEmails) { email ->
                        val cu = currentUserEmail
                        if (cu == null || email != cu) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Color(0xFFF0F0F0),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(email.value, modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier
                                        .clickable { if (!creating) viewModel.removeEmail(email) }
                                        .padding(4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = emailInput,
                onValueChange = { if (!creating) viewModel.onEmailInputChange(it) },
                label = { Text("Add member by email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                isError = emailError != null,
                enabled = !creating,
                trailingIcon = {
                    val cu = currentUserEmail
                    if (!creating && emailInput.isNotBlank() && cu != null && Email(emailInput) != cu) {
                        IconButton(onClick = {
                            viewModel.addEmail(cu)
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add email")
                        }
                    }
                },
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (!creating) currentUserEmail?.let { viewModel.addEmail(it) }
                        focusManager.clearFocus()
                    }
                )
            )

            if (emailError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = emailError ?: "Invalid email",
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { viewModel.dismissEmailError() }) {
                            Text("Dismiss", color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }
            }
        }
    }
}
