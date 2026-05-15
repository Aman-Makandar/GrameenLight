package com.grameenlight.domain.usecase

import com.grameenlight.domain.repository.EnergyRepository
import javax.inject.Inject

class GetEnergySavedUseCase @Inject constructor(
    private val energyRepository: EnergyRepository
) {
    suspend operator fun invoke(): Double {
        return energyRepository.getEnergySavedThisMonth()
    }
}
