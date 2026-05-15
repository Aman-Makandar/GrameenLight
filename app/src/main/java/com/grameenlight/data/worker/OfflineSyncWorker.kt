package com.grameenlight.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.FirebaseDatabase
import com.grameenlight.domain.repository.ComplaintRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class OfflineSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val complaintRepository: ComplaintRepository,
    private val firebaseDatabase: FirebaseDatabase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val unsyncedComplaints = complaintRepository.getUnsyncedComplaints()
            if (unsyncedComplaints.isEmpty()) {
                return Result.success()
            }

            val complaintsRef = firebaseDatabase.getReference("complaints")
            
            val syncedIds = mutableListOf<String>()
            for (complaint in unsyncedComplaints) {
                val updatedComplaint = complaint.copy(isSynced = true)
                complaintsRef.child(complaint.complaintId).setValue(updatedComplaint).await()
                syncedIds.add(complaint.complaintId)
            }

            if (syncedIds.isNotEmpty()) {
                complaintRepository.markAsSynced(syncedIds)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
    
    companion object {
        const val WORK_NAME = "OfflineSyncWorker"
    }
}
