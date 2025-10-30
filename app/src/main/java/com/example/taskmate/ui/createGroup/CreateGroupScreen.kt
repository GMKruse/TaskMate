package com.example.taskmate.ui.createGroup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import com.example.taskmate.models.DataState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    viewModel: CreateGroupViewModel,
    onGroupCreated: () -> Unit
) {
    val viewState by viewModel.viewState.collectAsState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = viewState.groupName,
                onValueChange = { if (!viewState.creatingGroup) viewModel.onGroupNameChange(it) },
                label = { Text("Group name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !viewState.creatingGroup,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (viewState.memberEmails.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 120.dp)
                ) {
                    items(viewState.memberEmails) { email ->
                        if (email != viewState.currentUser.email) {
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
                                        .clickable { if (!viewState.creatingGroup) viewModel.removeEmail(email) }
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
                value = viewState.emailInput,
                onValueChange = { if (!viewState.creatingGroup) viewModel.onEmailInputChange(it) },
                label = { Text("Add member by email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                isError = viewState.emailError != null,
                enabled = !viewState.creatingGroup,
                trailingIcon = {
                    if (!viewState.creatingGroup && viewState.emailInput.isNotBlank() && Email(viewState.emailInput) != viewState.currentUser.email) {
                        IconButton(onClick = {
                            viewModel.addEmail(viewState.currentUser.email)
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add email")
                        }
                    }
                },
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (!viewState.creatingGroup) viewModel.addEmail(viewState.currentUser.email)
                        focusManager.clearFocus()
                    }
                )
            )

            if (viewState.emailError != null) {
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
                            text = viewState.emailError!!,
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

        // Bottom button section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    viewModel.createGroup(viewState.currentUser.email) { success ->
                        if (success) onGroupCreated()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !viewState.creatingGroup && viewState.groupName.isNotBlank()
            ) {
                if (viewState.creatingGroup) {
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
}
