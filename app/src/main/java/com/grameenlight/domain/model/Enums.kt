package com.grameenlight.domain.model

enum class PoleStatus {
    WORKING, FUSED, BURNING_DAY, UNKNOWN
}

enum class RepairStatus {
    SUBMITTED, ASSIGNED, IN_PROGRESS, FIXED
}

enum class UserRole {
    RESIDENT, LINEMAN, ADMIN
}
