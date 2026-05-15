package com.grameenlight.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.grameenlight.domain.model.Complaint
import com.grameenlight.domain.model.PoleStatus
import com.grameenlight.domain.model.RepairStatus

@Entity(
    tableName = "complaints",
    foreignKeys = [
        ForeignKey(
            entity = PoleEntity::class,
            parentColumns = ["poleId"],
            childColumns = ["poleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("poleId")]
)
data class ComplaintEntity(
    @PrimaryKey
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
) {
    fun toDomain() = Complaint(
        complaintId = complaintId,
        poleId = poleId,
        reportedStatus = reportedStatus,
        photoUrl = photoUrl,
        reportedAt = reportedAt,
        reportedBy = reportedBy,
        reporterName = reporterName,
        assignedTo = assignedTo,
        assignedLinamanName = assignedLinamanName,
        repairStatus = repairStatus,
        resolvedAt = resolvedAt,
        isSynced = isSynced
    )
}

fun Complaint.toEntity() = ComplaintEntity(
    complaintId = complaintId,
    poleId = poleId,
    reportedStatus = reportedStatus,
    photoUrl = photoUrl,
    reportedAt = reportedAt,
    reportedBy = reportedBy,
    reporterName = reporterName,
    assignedTo = assignedTo,
    assignedLinamanName = assignedLinamanName,
    repairStatus = repairStatus,
    resolvedAt = resolvedAt,
    isSynced = isSynced
)
