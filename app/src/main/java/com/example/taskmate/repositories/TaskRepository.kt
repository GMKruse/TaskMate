package com.example.taskmate.repositories

import com.example.taskmate.models.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

interface ITaskRepository {
    fun createTask(task: Task, onComplete: (Boolean, String?) -> Unit)
    /**
     * Lytter i real-time på tasks for en gruppe.
     * Returnerer en funktion som kan kaldes for at fjerne listeneren.
     */
    fun listenToTasksForGroup(groupId: String, onResult: (List<Task>) -> Unit): () -> Unit
    fun fetchTasksForGroup(groupId: String, onResult: (List<Task>) -> Unit) // behold for kompatibilitet
}

class TaskRepository : ITaskRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val tasksRef = db.collection("tasks")

    override fun createTask(task: Task, onComplete: (Boolean, String?) -> Unit) {
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

    override fun listenToTasksForGroup(groupId: String, onResult: (List<Task>) -> Unit): () -> Unit {
        val listener: ListenerRegistration = tasksRef
            .whereEqualTo("groupId", groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // On fejl: returner tom liste (eller håndter fejlen efter behov)
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }

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

        // Returner unsubscribe-funktion
        return { listener.remove() }
    }
}
