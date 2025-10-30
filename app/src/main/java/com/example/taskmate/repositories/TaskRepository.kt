package com.example.taskmate.repositories
import com.google.firebase.firestore.FirebaseFirestore
import com.example.taskmate.models.Task
import kotlinx.coroutines.tasks.await

interface ITaskRepository {
    fun createTask(task: Task, onComplete: (Boolean, String?) -> Unit)
    fun fetchTasksForGroup(groupId: String, onResult: (List<Task>) -> Unit)
    suspend fun getTaskById(taskId: String): Task?
    suspend fun updateTaskCompletion(taskId: String, isCompleted: Boolean): Boolean
}

class TaskRepository : ITaskRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val tasksRef = db.collection("tasks")

    override fun createTask(task: Task, onComplete: (Boolean, String?) -> Unit) {
        // Hvis id er sat, brug det som document id, ellers lad Firestore generere et id
        val docRef = if (task.id.isNotBlank()) tasksRef.document(task.id) else tasksRef.document()

        val taskMap = hashMapOf(
            "id" to docRef.id,
            "name" to task.name,
            "description" to task.description,
            "isCompleted" to task.isCompleted,
            "groupId" to task.groupId
        )

        docRef.set(taskMap)
            .addOnSuccessListener { onComplete(true, docRef.id) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    override fun fetchTasksForGroup(groupId: String, onResult: (List<Task>) -> Unit) {
        tasksRef
            .whereEqualTo("groupId", groupId)
            .get()
            .addOnSuccessListener { snapshot ->
                val tasks = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val name = doc.getString("name") ?: ""
                    val description = doc.getString("description") ?: ""
                    val isCompleted = doc.getBoolean("isCompleted") ?: false
                    val docGroupId = doc.getString("groupId") ?: ""
                    Task(
                        id = id,
                        name = name,
                        description = description,
                        isCompleted = isCompleted,
                        groupId = docGroupId
                    )
                }
                onResult(tasks)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    override suspend fun getTaskById(taskId: String): Task? {
        return try {
            val doc = tasksRef.document(taskId).get().await()
            if (doc.exists()) {
                val id = doc.getString("id") ?: doc.id
                val name = doc.getString("name") ?: ""
                val description = doc.getString("description") ?: ""
                val isCompleted = doc.getBoolean("isCompleted") ?: false
                val groupId = doc.getString("groupId") ?: ""
                Task(
                    id = id,
                    name = name,
                    description = description,
                    isCompleted = isCompleted,
                    groupId = groupId
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateTaskCompletion(taskId: String, isCompleted: Boolean): Boolean {
        return try {
            tasksRef.document(taskId).update("isCompleted", isCompleted).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
