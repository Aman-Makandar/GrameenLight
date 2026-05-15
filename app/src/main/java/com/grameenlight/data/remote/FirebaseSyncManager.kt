package com.grameenlight.data.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.grameenlight.data.local.ComplaintDao
import com.grameenlight.data.local.PoleDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSyncManager @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val poleDao: PoleDao,
    private val complaintDao: ComplaintDao
) {
    private val polesRef = firebaseDatabase.getReference("poles")
    private val complaintsRef = firebaseDatabase.getReference("complaints")
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun startListening() {
        polesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                coroutineScope.launch {
                    val poles = snapshot.children.mapNotNull { child ->
                        try {
                            val id = child.child("poleId").getValue(String::class.java) ?: return@mapNotNull null
                            val streetName = child.child("streetName").getValue(String::class.java) ?: ""
                            val latitude = child.child("latitude").getValue(Double::class.java) ?: 0.0
                            val longitude = child.child("longitude").getValue(Double::class.java) ?: 0.0
                            val statusStr = child.child("currentStatus").getValue(String::class.java) ?: "UNKNOWN"
                            val lastUpdatedAt = child.child("lastUpdatedAt").getValue(Long::class.java) ?: 0L
                            val lastReportedBy = child.child("lastReportedBy").getValue(String::class.java) ?: ""
                            
                            com.grameenlight.data.local.entity.PoleEntity(
                                poleId = id,
                                streetName = streetName,
                                latitude = latitude,
                                longitude = longitude,
                                currentStatus = com.grameenlight.domain.model.PoleStatus.valueOf(statusStr),
                                lastUpdatedAt = lastUpdatedAt,
                                lastReportedBy = lastReportedBy
                            )
                        } catch(e: Exception) { null }
                    }
                    if (poles.isNotEmpty()) {
                        poleDao.upsertPoles(poles)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        complaintsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                coroutineScope.launch {
                    val complaints = snapshot.children.mapNotNull { child ->
                        try {
                            val id = child.child("complaintId").getValue(String::class.java) ?: return@mapNotNull null
                            val poleId = child.child("poleId").getValue(String::class.java) ?: return@mapNotNull null
                            val reportedStatusStr = child.child("reportedStatus").getValue(String::class.java) ?: "UNKNOWN"
                            val photoUrl = child.child("photoUrl").getValue(String::class.java)
                            val reportedAt = child.child("reportedAt").getValue(Long::class.java) ?: 0L
                            val reportedBy = child.child("reportedBy").getValue(String::class.java) ?: ""
                            val assignedTo = child.child("assignedTo").getValue(String::class.java)
                            val repairStatusStr = child.child("repairStatus").getValue(String::class.java) ?: "SUBMITTED"
                            val resolvedAt = child.child("resolvedAt").getValue(Long::class.java)
                            
                            com.grameenlight.data.local.entity.ComplaintEntity(
                                complaintId = id,
                                poleId = poleId,
                                reportedStatus = com.grameenlight.domain.model.PoleStatus.valueOf(reportedStatusStr),
                                photoUrl = photoUrl,
                                reportedAt = reportedAt,
                                reportedBy = reportedBy,
                                assignedTo = assignedTo,
                                repairStatus = com.grameenlight.domain.model.RepairStatus.valueOf(repairStatusStr),
                                resolvedAt = resolvedAt,
                                isSynced = true
                            )
                        } catch(e: Exception) { null }
                    }
                    if (complaints.isNotEmpty()) {
                        complaintDao.insertComplaints(complaints)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
