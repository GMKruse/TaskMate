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
import com.example.taskmate.managers.userManager.IUserManager
import com.example.taskmate.managers.userManager.UserManager
import com.example.taskmate.repositories.UserRepository
import com.example.taskmate.services.QuoteService
import com.example.taskmate.ui.groupOverview.GroupOverviewScreen
import com.example.taskmate.ui.createGroup.CreateGroupScreen
import com.example.taskmate.ui.taskOverview.TaskOverview // <- import for your TaskOverview
import com.example.taskmate.repositories.TaskRepository
import com.example.taskmate.repositories.ITaskRepository
import com.example.taskmate.ui.specificTask.SpecificTaskScreen
import com.example.taskmate.ui.specificTask.SpecificTaskViewModel

sealed class AppRoute(val route: String) {
    object GroupOverview : AppRoute("group_overview")
    object CreateGroup : AppRoute("create_group")
    object TaskOverview : AppRoute("task_overview/{groupId}") {
        fun createRoute(groupId: String) = "task_overview/$groupId"
    }
    object SpecificTask : AppRoute("task_details/{taskId}") {
        fun createRoute(taskId: String) = "task_details/$taskId"
    }
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
                                val userManager = UserManager.getInstance()
                                val groupRepository: IGroupRepository = GroupRepository()
                                val quoteService = QuoteService()

                                @Suppress("UNCHECKED_CAST")
                                return GroupOverviewViewModel(userManager, groupRepository, quoteService) as T
                            }
                        }
                    )


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
                            navController.navigate(AppRoute.TaskOverview.createRoute(groupId))
                        }
                    )
                }

                composable(AppRoute.CreateGroup.route) {
                    val viewModel: CreateGroupViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                val userManager = UserManager.getInstance()
                                val groupRepository: IGroupRepository = GroupRepository()

                                @Suppress("UNCHECKED_CAST")
                                return CreateGroupViewModel(userManager, groupRepository) as T
                            }
                        }
                    )

                    LaunchedEffect(Unit) {
                        topBarConfig = TopBarConfig(
                            title = "New group",
                            showNavigationIcon = true,
                            onNavigationClick = { navController.popBackStack() }
                        )
                    }

                    CreateGroupScreen(
                        viewModel = viewModel,
                        onGroupCreated = {
                            navController.popBackStack()
                        }
                    )
                }


                composable(AppRoute.TaskOverview.route) { backStackEntry ->
                    val groupId = backStackEntry.arguments?.getString("groupId") ?: ""

                    LaunchedEffect(Unit) {
                        topBarConfig = TopBarConfig(
                            title = "Group Tasks",
                            showNavigationIcon = true,
                            onNavigationClick = { navController.popBackStack() }
                        )
                    }

                    TaskOverview(
                        groupId = groupId,
                        onTaskClick = { task ->
                            navController.navigate(AppRoute.SpecificTask.createRoute(task.id))
                        }
                    )
                }

                composable(AppRoute.SpecificTask.route) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                    val taskRepository: ITaskRepository = TaskRepository()
                    val viewModel: SpecificTaskViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return SpecificTaskViewModel(taskId, taskRepository) as T
                            }
                        }
                    )

                    LaunchedEffect(Unit) {
                        topBarConfig = TopBarConfig(
                            title = "Task Details",
                            showNavigationIcon = true,
                            onNavigationClick = { navController.popBackStack() }
                        )
                    }

                    SpecificTaskScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
