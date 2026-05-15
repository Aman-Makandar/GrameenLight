package com.grameenlight.domain.usecase

import com.grameenlight.domain.model.Pole
import com.grameenlight.domain.repository.PoleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPolesUseCase @Inject constructor(
    private val poleRepository: PoleRepository
) {
    operator fun invoke(): Flow<List<Pole>> {
        return poleRepository.getPoles()
    }

    suspend fun syncFromFirebase() {
        poleRepository.syncPolesFromFirebase()
    }
}
