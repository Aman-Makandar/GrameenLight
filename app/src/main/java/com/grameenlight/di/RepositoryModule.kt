package com.grameenlight.di

import com.grameenlight.data.repository.*
import com.grameenlight.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPoleRepository(
        poleRepositoryImpl: PoleRepositoryImpl
    ): PoleRepository

    @Binds
    @Singleton
    abstract fun bindComplaintRepository(
        complaintRepositoryImpl: ComplaintRepositoryImpl
    ): ComplaintRepository

    @Binds
    @Singleton
    abstract fun bindEnergyRepository(
        energyRepositoryImpl: EnergyRepositoryImpl
    ): EnergyRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(
        storageRepositoryImpl: StorageRepositoryImpl
    ): StorageRepository
}
