package com.example.taskmate.gate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.example.taskmate.ui.usable.LoadingScreen
import com.example.taskmate.managers.userManager.IUserManager
import com.example.taskmate.managers.userManager.UserManagerState
import com.example.taskmate.navigation.AuthNavHost
import com.example.taskmate.navigation.AppNavHost

@Composable
fun LoginGate(userManager: IUserManager) {
    val state by userManager.state.collectAsState()
    val scope = rememberCoroutineScope()

    when (state) {
        is UserManagerState.Loading -> {
            LoadingScreen()
        }

        is UserManagerState.LoggedOut -> {
            val error = (state as UserManagerState.LoggedOut).error
            AuthNavHost(
                userManager = userManager,
                error = error,
                scope = scope
            )
        }

        is UserManagerState.LoggedIn -> {
            AppNavHost(userManager)
        }
    }
}