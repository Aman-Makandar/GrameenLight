package com.grameenlight.domain.model

data class Complaint(
    val complaintId: String,
    val poleId: String,
    val reportedStatus: PoleStatus,
    val photoUrl: String?,
    val reportedAt: Long,
    val reportedBy: String,
    val reporterName: String = "",
    val assignedTo: String?,
    val assignedLinamanName: String? = null,
    val repairStatus: RepairStatus,
    val resolvedAt: Long?,
    val isSynced: Boolean
)
