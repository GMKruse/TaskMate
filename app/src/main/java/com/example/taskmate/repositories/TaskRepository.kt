package com.example.taskmate.repositories
import com.google.firebase.firestore.FirebaseFirestore
import com.example.taskmate.models.Task

interface ITaskRepository {
    fun createTask(task: Task, onComplete: (Boolean, String?) -> Unit)
    fun fetchTasksForGroup(groupId: String, onResult: (List<Task>) -> Unit)
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
}
