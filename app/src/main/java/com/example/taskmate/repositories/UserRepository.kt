package com.example.taskmate.repositories

import android.content.Context
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface IUserRepository {
    suspend fun getCurrentUserName(): String?
    fun logout()
}

class UserRepository(private val context: Context) : IUserRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun login(email: String, password: String): Result<AuthResult> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<AuthResult> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User not created")

            val userDoc = mapOf(
                "name" to name,
                "email" to email,
                "groups" to emptyList<String>(),
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(user.uid).set(userDoc).await()

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun logout() {
        auth.signOut()
    }

    override suspend fun getCurrentUserName(): String? {
        val uid = getCurrentUserId() ?: return null
        val doc = db.collection("users").document(uid).get().await()
        return doc.getString("name")
    }
}