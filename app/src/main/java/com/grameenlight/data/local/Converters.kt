package com.grameenlight.data.local

import androidx.room.TypeConverter
import com.grameenlight.domain.model.PoleStatus
import com.grameenlight.domain.model.RepairStatus
import com.grameenlight.domain.model.UserRole

class Converters {
    @TypeConverter
    fun fromPoleStatus(status: PoleStatus): String {
        return status.name
    }

    @TypeConverter
    fun toPoleStatus(status: String): PoleStatus {
        return try {
            PoleStatus.valueOf(status)
        } catch (e: Exception) {
            PoleStatus.UNKNOWN
        }
    }

    @TypeConverter
    fun fromRepairStatus(status: RepairStatus): String {
        return status.name
    }

    @TypeConverter
    fun toRepairStatus(status: String): RepairStatus {
        return try {
            RepairStatus.valueOf(status)
        } catch (e: Exception) {
            RepairStatus.SUBMITTED
        }
    }

    @TypeConverter
    fun fromUserRole(role: UserRole): String {
        return role.name
    }

    @TypeConverter
    fun toUserRole(role: String): UserRole {
        return try {
            UserRole.valueOf(role)
        } catch (e: Exception) {
            UserRole.RESIDENT
        }
    }
}
