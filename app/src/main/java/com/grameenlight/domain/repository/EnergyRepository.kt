package com.grameenlight.domain.repository

interface EnergyRepository {
    suspend fun getEnergySavedThisMonth(): Double
    suspend fun getVillageRanking(): Int
}
