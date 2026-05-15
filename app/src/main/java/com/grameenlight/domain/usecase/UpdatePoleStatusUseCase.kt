package com.grameenlight.domain.usecase

import com.grameenlight.domain.model.PoleStatus
import com.grameenlight.domain.repository.PoleRepository
import javax.inject.Inject

class UpdatePoleStatusUseCase @Inject constructor(
    private val poleRepository: PoleRepository
) {
    suspend operator fun invoke(poleId: String, status: PoleStatus) {
        poleRepository.updatePoleStatus(poleId, status)
    }
}
