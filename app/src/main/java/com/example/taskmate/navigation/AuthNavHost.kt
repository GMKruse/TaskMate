package com.example.taskmate.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taskmate.managers.IUserManager
import com.example.taskmate.ui.LoginScreen
import com.example.taskmate.ui.RegisterScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class AuthRoute(val route: String) {
    object Login : AuthRoute("login")
    object Register : AuthRoute("register")
}

@Composable
fun AuthNavHost(
    userManager: IUserManager,
    error: String?,
    scope: CoroutineScope
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthRoute.Login.route
    ) {
        composable(AuthRoute.Login.route) {
            LoginScreen(
                error = error,
                onLogin = { email, password ->
                    scope.launch {
                        userManager.login(email, password)
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(AuthRoute.Register.route)
                }
            )
        }

        composable(AuthRoute.Register.route) {
            RegisterScreen(
                error = error,
                onRegister = { name, email, password ->
                    scope.launch {
                        userManager.register(name, email, password)
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(AuthRoute.Login.route) {
                        popUpTo(AuthRoute.Login.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
