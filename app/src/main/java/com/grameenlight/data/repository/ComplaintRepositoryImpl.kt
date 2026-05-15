package com.grameenlight.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.grameenlight.data.local.ComplaintDao
import com.grameenlight.data.local.entity.toEntity
import com.grameenlight.domain.model.Complaint
import com.grameenlight.domain.model.RepairStatus
import com.grameenlight.domain.repository.ComplaintRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ComplaintRepositoryImpl @Inject constructor(
    private val complaintDao: ComplaintDao,
    private val firebaseDatabase: FirebaseDatabase
) : ComplaintRepository {

    private val complaintsRef = firebaseDatabase.getReference("complaints")

    override suspend fun submitComplaint(complaint: Complaint) {
        try {
            android.util.Log.d("SUBMIT", "Starting repository submission for: ${complaint.complaintId}")
            complaintDao.insertComplaint(complaint.toEntity())
            android.util.Log.d("SUBMIT", "Saved to Room DB successfully")
            
            complaintsRef.child(complaint.complaintId).setValue(complaint).await()
            android.util.Log.d("SUBMIT", "Synced to Firebase successfully")
        } catch (e: Exception) {
            android.util.Log.e("SUBMIT", "Error in submitComplaint: ${e.message}", e)
            throw e
        }
    }

    override fun getComplaints(): Flow<List<Complaint>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val complaints = snapshot.children.mapNotNull { child ->
                    try {
                        val id = child.child("complaintId").getValue(String::class.java) ?: return@mapNotNull null
                        val poleId = child.child("poleId").getValue(String::class.java) ?: return@mapNotNull null
                        val reportedStatusStr = child.child("reportedStatus").getValue(String::class.java) ?: "UNKNOWN"
                        val photoUrl = child.child("photoUrl").getValue(String::class.java)
                        val reportedAt = child.child("reportedAt").getValue(Long::class.java) ?: 0L
                        val reportedBy = child.child("reportedBy").getValue(String::class.java) ?: ""
                        val reporterName = child.child("reporterName").getValue(String::class.java) ?: ""
                        val assignedTo = child.child("assignedTo").getValue(String::class.java)
                        val assignedLinamanName = child.child("assignedLinamanName").getValue(String::class.java)
                        val repairStatusStr = child.child("repairStatus").getValue(String::class.java) ?: "SUBMITTED"
                        val resolvedAt = child.child("resolvedAt").getValue(Long::class.java)
                        
                        Complaint(
                            complaintId = id,
                            poleId = poleId,
                            reportedStatus = com.grameenlight.domain.model.PoleStatus.valueOf(reportedStatusStr),
                            photoUrl = photoUrl,
                            reportedAt = reportedAt,
                            reportedBy = reportedBy,
                            reporterName = reporterName,
                            assignedTo = assignedTo,
                            assignedLinamanName = assignedLinamanName,
                            repairStatus = com.grameenlight.domain.model.RepairStatus.valueOf(repairStatusStr),
                            resolvedAt = resolvedAt,
                            isSynced = true
                        )
                    } catch(e: Exception) { null }
                }
                trySend(complaints)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        complaintsRef.addValueEventListener(listener)
        awaitClose { complaintsRef.removeEventListener(listener) }
    }

    override suspend fun updateRepairStatus(complaintId: String, status: RepairStatus, resolvedAt: Long?) {
        complaintDao.updateRepairStatus(complaintId, status, resolvedAt)
        complaintsRef.child(complaintId).child("repairStatus").setValue(status.name).await()
        if (resolvedAt != null) {
            complaintsRef.child(complaintId).child("resolvedAt").setValue(resolvedAt).await()
        }
    }

    override suspend fun syncComplaintsFromFirebase() {
        // This is now handled by real-time listener in getComplaints
    }

    override suspend fun getUnsyncedComplaints(): List<Complaint> {
        return complaintDao.getUnsyncedComplaints().map { it.toDomain() }
    }
    
    override suspend fun markAsSynced(complaintIds: List<String>) {
        complaintDao.markAsSynced(complaintIds)
    }
}
