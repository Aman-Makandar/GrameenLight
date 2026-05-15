package com.grameenlight.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.grameenlight.data.local.UserDao
import com.grameenlight.data.local.entity.UserEntity
import com.grameenlight.data.local.entity.toEntity
import com.grameenlight.domain.model.User
import com.grameenlight.domain.model.UserRole
import com.grameenlight.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

import com.grameenlight.data.local.UserPreferences
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) : AuthRepository {

    private val usersRef = firebaseDatabase.getReference("users")

    override suspend fun register(name: String, email: String, password: String, role: UserRole): Result<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Registration failed")
            
            val user = User(
                uid = firebaseUser.uid,
                name = name,
                email = email,
                role = role,
                createdAt = System.currentTimeMillis()
            )
            
            // Save to Firebase Realtime DB
            usersRef.child(firebaseUser.uid).setValue(user).await()
            
            // Save UID to DataStore
            userPreferences.saveUserId(firebaseUser.uid)
            
            // Sync to local DB
            userDao.insertUser(user.toEntity())
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Login failed")
            
            // Fetch user details from Firebase to get the role
            val snapshot = usersRef.child(firebaseUser.uid).get().await()
            val user = snapshot.getValue(User::class.java) ?: throw Exception("User data not found")
            
            // Save UID to DataStore
            userPreferences.saveUserId(firebaseUser.uid)
            
            // Sync to local DB
            userDao.insertUser(user.toEntity())
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        val uid = userPreferences.userIdFlow.firstOrNull() ?: firebaseAuth.currentUser?.uid ?: return null
        
        return userDao.getUserById(uid)?.toDomain() ?: run {
            try {
                val snapshot = usersRef.child(uid).get().await()
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    userDao.insertUser(user.toEntity())
                }
                user
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun getAllLinemen(): Result<List<User>> {
        return try {
            val snapshot = usersRef.get().await()
            val linemen = snapshot.children.mapNotNull { child ->
                val roleStr = child.child("role").getValue(String::class.java)
                if (roleStr == UserRole.LINEMAN.name) {
                    child.getValue(User::class.java)
                } else null
            }
            Result.success(linemen)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun signOut() {
        firebaseAuth.signOut()
        kotlinx.coroutines.GlobalScope.launch {
            userPreferences.clearUserId()
        }
    }
}
