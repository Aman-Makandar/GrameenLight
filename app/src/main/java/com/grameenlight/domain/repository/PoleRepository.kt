package com.grameenlight.domain.repository

import com.grameenlight.domain.model.Pole
import com.grameenlight.domain.model.PoleStatus
import kotlinx.coroutines.flow.Flow

interface PoleRepository {
    fun getPoles(): Flow<List<Pole>>
    suspend fun getPoleById(poleId: String): Pole?
    suspend fun updatePoleStatus(poleId: String, status: PoleStatus)
    suspend fun syncPolesFromFirebase()
}
