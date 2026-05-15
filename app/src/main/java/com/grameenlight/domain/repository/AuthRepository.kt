package com.grameenlight.domain.repository

import com.grameenlight.domain.model.User
import com.grameenlight.domain.model.UserRole

interface AuthRepository {
    suspend fun register(name: String, email: String, password: String, role: UserRole): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun getCurrentUser(): User?
    fun isLoggedIn(): Boolean
    fun signOut()
    suspend fun getAllLinemen(): Result<List<User>>
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}
