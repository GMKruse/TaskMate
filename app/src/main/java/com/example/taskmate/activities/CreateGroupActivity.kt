package com.example.taskmate.activities

import GroupRepository
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.taskmate.models.Group
import com.example.taskmate.ui.theme.TaskMateTheme
import com.example.taskmate.repositories.UserRepository
import com.example.taskmate.models.Email
import com.example.taskmate.models.User

@OptIn(ExperimentalMaterial3Api::class)
class CreateGroupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskMateTheme {
                CreateGroupScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(onBack: () -> Unit) {
    var groupName by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var memberEmails by remember { mutableStateOf(listOf<Email>()) }
    var isLoading by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val groupRepository = remember { GroupRepository() }
    val userRepository = remember { UserRepository() }
    var currentUser by remember { mutableStateOf<User?>(null) }

    // Fetch current user email and id once
    LaunchedEffect(Unit) {
        currentUser = userRepository.getCurrentUser()
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
                        if (currentUser == null) {
                            isLoading = false
                            return@Button
                        }
                        isLoading = true
                        val filteredMembers = memberEmails.filter { it != currentUser!!.email }
                        // Ensure current user email is in the members list
                        val members = (filteredMembers + currentUser!!.email).distinct()
                        val group = Group(
                            name = groupName,
                            createdBy = currentUser!!.id,
                            members = members,
                            createdAt = System.currentTimeMillis()
                        )
                        groupRepository.createGroup(group) { success, _ ->
                            isLoading = false
                            if (success) {
                                onBack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading && groupName.isNotBlank() && currentUser != null
                ) {
                    if (isLoading) {
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
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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
                        if (email != currentUser!!.email) {
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
                                        .clickable {
                                            memberEmails = memberEmails - email
                                        }
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
                onValueChange = {
                    emailInput = it
                    emailError = false
                },
                label = { Text("Add member by email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                isError = emailError,
                trailingIcon = {
                    if (emailInput.isNotBlank() && Email(emailInput) != currentUser!!.email) {
                        IconButton(onClick = {
                            val emailObj = Email(emailInput)
                            if (isValidEmail(emailInput) && emailObj !in memberEmails && emailObj != currentUser!!.email) {
                                memberEmails = memberEmails + emailObj
                                emailInput = ""
                                emailError = false
                                focusManager.clearFocus()
                            } else {
                                emailError = true
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add email")
                        }
                    }
                },
                keyboardActions = KeyboardActions(
                    onDone = {
                        val emailObj = Email(emailInput)
                        if (emailInput.isNotBlank() && emailObj != currentUser!!.email) {
                            if (isValidEmail(emailInput) && emailObj !in memberEmails) {
                                memberEmails = memberEmails + emailObj
                                emailInput = ""
                                emailError = false
                                focusManager.clearFocus()
                            } else {
                                emailError = true
                            }
                        } else {
                            emailInput = ""
                            emailError = false
                            focusManager.clearFocus()
                        }
                    }
                )
            )
            if (emailError) {
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
                            text = "Invalid email",
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { emailError = false }) {
                            Text("Dismiss", color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }
            }
        }
    }
}

// Helper function for email validation
fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
