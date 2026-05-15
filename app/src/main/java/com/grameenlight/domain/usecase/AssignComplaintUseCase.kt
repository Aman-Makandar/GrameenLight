package com.grameenlight.domain.usecase

import com.google.firebase.database.FirebaseDatabase
import com.grameenlight.domain.model.RepairStatus
import com.grameenlight.domain.repository.ComplaintRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AssignComplaintUseCase @Inject constructor(
    private val complaintRepository: ComplaintRepository,
    private val firebaseDatabase: FirebaseDatabase
) {
    suspend operator fun invoke(
        complaintId: String,
        linemanId: String,
        linemanName: String
    ): Result<Unit> {
        return try {
            val ref = firebaseDatabase.getReference("complaints").child(complaintId)
            val updates = mapOf(
                "assignedTo" to linemanId,
                "assignedLinamanName" to linemanName,
                "repairStatus" to RepairStatus.ASSIGNED.name
            )
            ref.updateChildren(updates).await()
            // Also update local Room cache
            complaintRepository.updateRepairStatus(complaintId, RepairStatus.ASSIGNED, null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
