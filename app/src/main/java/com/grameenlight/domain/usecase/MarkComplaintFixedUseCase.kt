package com.grameenlight.domain.usecase

import com.google.firebase.database.FirebaseDatabase
import com.grameenlight.domain.model.RepairStatus
import com.grameenlight.domain.repository.ComplaintRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MarkComplaintFixedUseCase @Inject constructor(
    private val complaintRepository: ComplaintRepository,
    private val firebaseDatabase: FirebaseDatabase
) {
    suspend operator fun invoke(complaintId: String): Result<Unit> {
        return try {
            val resolvedAt = System.currentTimeMillis()
            complaintRepository.updateRepairStatus(complaintId, RepairStatus.FIXED, resolvedAt)
            firebaseDatabase.getReference("complaints").child(complaintId).child("repairStatus").setValue(RepairStatus.FIXED.name).await()
            firebaseDatabase.getReference("complaints").child(complaintId).child("resolvedAt").setValue(resolvedAt).await()
            
            // Also update the pole status to WORKING on the map
            val poleIdSnapshot = firebaseDatabase.getReference("complaints").child(complaintId).child("poleId").get().await()
            val poleId = poleIdSnapshot.getValue(String::class.java)
            if (poleId != null) {
                firebaseDatabase.getReference("poles").child(poleId).child("currentStatus").setValue("WORKING").await()
                firebaseDatabase.getReference("poles").child(poleId).child("lastUpdatedAt").setValue(resolvedAt).await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
