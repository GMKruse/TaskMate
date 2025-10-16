package com.example.taskmate.repositories

import android.content.Context
import com.example.taskmate.models.User
import com.example.taskmate.models.UserId
import com.example.taskmate.models.Email
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface IUserRepository {
    suspend fun getCurrentUser(): User?
    fun logout()
}

class UserRepository() : IUserRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun login(email: Email, password: String): Result<AuthResult> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.value, password).await()

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: Email, password: String): Result<AuthResult> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.value, password).await()
            val user = result.user ?: throw Exception("User not created")

            val userDoc = mapOf(
                "name" to name,
                "email" to email.value,
                "groups" to emptyList<String>(),
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(user.uid).set(userDoc).await()

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        auth.signOut()
    }

    override suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        val doc = db.collection("users").document(uid).get().await()
        val name = doc.getString("name") ?: return null
        val email = doc.getString("email") ?: return null
        return User(id = UserId(uid), email = Email(email), name = name)
    }
}