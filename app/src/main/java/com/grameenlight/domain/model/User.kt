package com.grameenlight.domain.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.RESIDENT,
    val createdAt: Long = 0L
)
