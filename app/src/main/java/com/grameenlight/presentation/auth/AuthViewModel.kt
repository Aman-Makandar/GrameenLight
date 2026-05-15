package com.grameenlight.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grameenlight.domain.model.User
import com.grameenlight.domain.model.UserRole
import com.grameenlight.domain.repository.AuthRepository
import com.grameenlight.domain.repository.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    // FIX: Add a dedicated SignedOut state so Navigation
    // can react and go to login screen
    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin: StateFlow<Boolean> = _navigateToLogin

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            val isLoggedIn = authRepository.isLoggedIn()
            if (isLoggedIn) {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    _isAuthenticated.value = true
                    _authState.value = AuthState.Success(user)
                } else {
                    _isAuthenticated.value = false
                    authRepository.signOut()
                    _navigateToLogin.value = true
                }
            } else {
                _isAuthenticated.value = false
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) return
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.login(email, password)
            result.onSuccess { user ->
                _isAuthenticated.value = true
                _navigateToLogin.value = false
                _authState.value = AuthState.Success(user)
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Login failed")
            }
        }
    }

    fun register(name: String, email: String, password: String, role: UserRole) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) return
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.register(name, email, password, role)
            result.onSuccess { user ->
                _isAuthenticated.value = true
                _navigateToLogin.value = false
                _authState.value = AuthState.Success(user)
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Registration failed")
            }
        }
    }

    // FIX: logout now emits navigateToLogin = true
    // so Navigation.kt can react and pop to login screen
    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            _isAuthenticated.value = false
            _authState.value = AuthState.Idle
            // FIX: This triggers navigation to login in MainAppNavigation
            _navigateToLogin.value = true
        }
    }

    // FIX: Call this after navigation has handled the logout
    // so it doesn't keep triggering
    fun onNavigatedToLogin() {
        _navigateToLogin.value = false
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}