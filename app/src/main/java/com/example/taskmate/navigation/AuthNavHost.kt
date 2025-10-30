package com.example.taskmate.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taskmate.managers.userManager.IUserManager
import com.example.taskmate.managers.userManager.UserManager
import com.example.taskmate.ui.LoginScreen
import com.example.taskmate.ui.registerScreen.RegisterScreen
import com.example.taskmate.ui.registerScreen.RegisterScreenViewModel
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
            val registerViewModel: RegisterScreenViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        val userManager = UserManager.getInstance()

                        @Suppress("UNCHECKED_CAST")
                        return RegisterScreenViewModel(userManager) as T
                    }
                }
            )

            RegisterScreen(
                viewModel = registerViewModel,
                onNavigateToLogin = {
                    navController.navigate(AuthRoute.Login.route) {
                        popUpTo(AuthRoute.Login.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    // After successful registration, navigate back to login
                    navController.navigate(AuthRoute.Login.route) {
                        popUpTo(AuthRoute.Login.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
