package com.grameenlight.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grameenlight.data.local.entity.ComplaintEntity
import com.grameenlight.domain.model.RepairStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplaintDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaints(complaints: List<ComplaintEntity>)

    @Query("SELECT * FROM complaints ORDER BY reportedAt DESC")
    fun getAllComplaints(): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE poleId = :poleId ORDER BY reportedAt DESC")
    fun getComplaintsByPole(poleId: String): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE repairStatus = :status ORDER BY reportedAt DESC")
    fun getComplaintsByStatus(status: RepairStatus): Flow<List<ComplaintEntity>>

    @Query("UPDATE complaints SET repairStatus = :status, resolvedAt = :resolvedAt, isSynced = 0 WHERE complaintId = :complaintId")
    suspend fun updateRepairStatus(complaintId: String, status: RepairStatus, resolvedAt: Long?)

    @Query("SELECT * FROM complaints WHERE isSynced = 0")
    suspend fun getUnsyncedComplaints(): List<ComplaintEntity>

    @Query("UPDATE complaints SET isSynced = 1 WHERE complaintId IN (:complaintIds)")
    suspend fun markAsSynced(complaintIds: List<String>)
}
