package com.grameenlight.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.grameenlight.domain.model.User
import com.grameenlight.domain.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uid: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val createdAt: Long
) {
    fun toDomain() = User(
        uid = uid,
        name = name,
        email = email,
        role = role,
        createdAt = createdAt
    )
}

fun User.toEntity() = UserEntity(
    uid = uid,
    name = name,
    email = email,
    role = role,
    createdAt = createdAt
)
