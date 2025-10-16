import com.example.taskmate.models.Email
import com.example.taskmate.models.Group
import com.example.taskmate.models.UserId
import com.google.firebase.firestore.FirebaseFirestore

interface IGroupRepository {
    fun createGroup(group: Group, onComplete: (Boolean, String?) -> Unit)
    fun fetchGroupsForUser(email: Email, onResult: (List<Group>) -> Unit)
}

class GroupRepository() : IGroupRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val groupsRef = db.collection("groups")

    override fun createGroup(group: Group, onComplete: (Boolean, String?) -> Unit) {
        val groupDoc = if (group.id.isNotBlank()) groupsRef.document(group.id) else groupsRef.document()
        val groupWithId = group.copy(id = groupDoc.id)

        val groupMap = hashMapOf(
            "id" to groupWithId.id,
            "name" to groupWithId.name,
            "createdBy" to groupWithId.createdBy.value,
            "members" to groupWithId.members.map { it.value },
            "createdAt" to groupWithId.createdAt
        )
        groupDoc.set(groupMap)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    override fun fetchGroupsForUser(email: Email, onResult: (List<Group>) -> Unit) {
        groupsRef.whereArrayContains("members", email.value)
            .get()
            .addOnSuccessListener { snapshot ->
                val groups = snapshot.documents.mapNotNull { doc ->
                    val id = doc.getString("id") ?: return@mapNotNull null
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val createdBy = doc.getString("createdBy") ?: return@mapNotNull null
                    val members = (doc.get("members") as? List<*>)?.mapNotNull { it as? String }?.map { Email(it) } ?: emptyList()
                    val createdAt = doc.getLong("createdAt") ?: 0L
                    Group(id, name, UserId(createdBy), members, createdAt)
                }
                onResult(groups)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}