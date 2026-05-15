package com.grameenlight.domain.repository

import com.grameenlight.domain.model.Complaint
import com.grameenlight.domain.model.RepairStatus
import kotlinx.coroutines.flow.Flow

interface ComplaintRepository {
    suspend fun submitComplaint(complaint: Complaint)
    fun getComplaints(): Flow<List<Complaint>>
    suspend fun updateRepairStatus(complaintId: String, status: RepairStatus, resolvedAt: Long?)
    suspend fun syncComplaintsFromFirebase()
    suspend fun getUnsyncedComplaints(): List<Complaint>
    suspend fun markAsSynced(complaintIds: List<String>)
}
