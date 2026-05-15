package com.grameenlight.domain.model

data class Pole(
    val poleId: String,
    val streetName: String,
    val latitude: Double,
    val longitude: Double,
    val currentStatus: PoleStatus,
    val lastUpdatedAt: Long,
    val lastReportedBy: String
)
