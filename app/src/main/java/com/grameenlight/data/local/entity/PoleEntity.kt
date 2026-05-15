package com.grameenlight.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.grameenlight.domain.model.PoleStatus
import com.grameenlight.domain.model.Pole

@Entity(tableName = "poles")
data class PoleEntity(
    @PrimaryKey
    val poleId: String,
    val streetName: String,
    val latitude: Double,
    val longitude: Double,
    val currentStatus: PoleStatus,
    val lastUpdatedAt: Long,
    val lastReportedBy: String
) {
    fun toDomain() = Pole(
        poleId = poleId,
        streetName = streetName,
        latitude = latitude,
        longitude = longitude,
        currentStatus = currentStatus,
        lastUpdatedAt = lastUpdatedAt,
        lastReportedBy = lastReportedBy
    )
}

fun Pole.toEntity() = PoleEntity(
    poleId = poleId,
    streetName = streetName,
    latitude = latitude,
    longitude = longitude,
    currentStatus = currentStatus,
    lastUpdatedAt = lastUpdatedAt,
    lastReportedBy = lastReportedBy
)
