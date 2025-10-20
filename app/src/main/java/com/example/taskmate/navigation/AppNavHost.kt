package com.example.taskmate.navigation

import GroupRepository
import IGroupRepository
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taskmate.ui.createGroup.CreateGroupViewModel
import com.example.taskmate.ui.groupOverview.GroupOverviewViewModel
import com.example.taskmate.managers.IUserManager
import com.example.taskmate.repositories.UserRepository
import com.example.taskmate.ui.groupOverview.GroupOverviewScreen
import com.example.taskmate.ui.createGroup.CreateGroupScreen

sealed class AppRoute(val route: String) {
    object GroupOverview : AppRoute("group_overview")
    object CreateGroup : AppRoute("create_group")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(userManager: IUserManager) {
    val navController = rememberNavController()
    var topBarConfig by remember { mutableStateOf<TopBarConfig?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ConfigurableTopAppBar(config = topBarConfig)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = AppRoute.GroupOverview.route
            ) {
                composable(AppRoute.GroupOverview.route) {
                    val viewModel: GroupOverviewViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                val userRepository = UserRepository()
                                val groupRepository: IGroupRepository = GroupRepository()
                                @Suppress("UNCHECKED_CAST")
                                return GroupOverviewViewModel(userRepository, groupRepository) as T
                            }
                        }
                    )

                    // Set TopBar configuration for this screen
                    LaunchedEffect(Unit) {
                        topBarConfig = TopBarConfig(
                            title = "Groups",
                            showNavigationIcon = false,
                            actions = {
                                TextButton(onClick = { userManager.logout() }) {
                                    Text("Log out")
                                }
                            }
                        )
                        viewModel.refreshGroups()
                    }

                    GroupOverviewScreen(
                        viewModel = viewModel,
                        onNavigateToCreateGroup = {
                            navController.navigate(AppRoute.CreateGroup.route)
                        },
                        onNavigateToGroupDetails = { groupId: String ->
                            // TODO: Navigate to group details screen
                        }
                    )
                }

                composable(AppRoute.CreateGroup.route) {
                    val viewModel: CreateGroupViewModel = viewModel()

                    // Set TopBar configuration for this screen
                    LaunchedEffect(Unit) {
                        topBarConfig = TopBarConfig(
                            title = "New group",
                            showNavigationIcon = true,
                            onNavigationClick = { navController.popBackStack() }
                        )
                    }

                    CreateGroupScreen(
                        viewModel = viewModel,
                        onBack = {
                            navController.popBackStack()
                        },
                        onGroupCreated = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
