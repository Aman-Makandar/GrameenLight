package com.grameenlight.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.grameenlight.data.local.PoleDao
import com.grameenlight.data.local.entity.toEntity
import com.grameenlight.domain.model.Pole
import com.grameenlight.domain.model.PoleStatus
import com.grameenlight.domain.repository.PoleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PoleRepositoryImpl @Inject constructor(
    private val poleDao: PoleDao,
    private val firebaseDatabase: FirebaseDatabase
) : PoleRepository {

    private val polesRef = firebaseDatabase.getReference("poles")

    override fun getPoles(): Flow<List<Pole>> {
        return poleDao.getAllPolesAsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPoleById(poleId: String): Pole? {
        return poleDao.getPoleById(poleId)?.toDomain()
    }

    override suspend fun updatePoleStatus(poleId: String, status: PoleStatus) {
        val pole = poleDao.getPoleById(poleId)
        if (pole != null) {
            val updated = pole.copy(currentStatus = status, lastUpdatedAt = System.currentTimeMillis())
            poleDao.upsertPole(updated)
            polesRef.child(poleId).setValue(updated.toDomain()).await()
        }
    }

    override suspend fun syncPolesFromFirebase() {
        try {
            val snapshot = polesRef.get().await()
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
