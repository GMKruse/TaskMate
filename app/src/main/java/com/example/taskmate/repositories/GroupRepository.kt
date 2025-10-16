import com.example.taskmate.models.Group
import com.google.firebase.database.*
import com.google.firebase.database.FirebaseDatabase

class GroupRepository(private val db: FirebaseDatabase = FirebaseDatabase.getInstance()) {
    private val groupsRef = db.getReference("groups")

    fun createGroup(group: Group, onComplete: (Boolean, String?) -> Unit) {
        val groupId = groupsRef.push().key ?: return onComplete(false, "Failed to generate groupId")
        val groupWithId = group.copy(id = groupId)
        groupsRef.child(groupId).setValue(groupWithId)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    fun fetchGroupsForUser(userId: String, onResult: (List<Group>) -> Unit) {
        groupsRef.orderByChild("members").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val groups = mutableListOf<Group>()
                for (groupSnap in snapshot.children) {
                    val group = groupSnap.getValue(Group::class.java)
                    if (group != null && group.members.contains(userId)) {
                        groups.add(group)
                    }
                }
                onResult(groups)
            }
            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }
        })
    }
}