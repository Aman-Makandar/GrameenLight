package com.grameenlight.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    // Note: Most UseCases in this project use constructor injection (@Inject constructor)
    // and do not require explicit @Provides or @Binds methods. 
    // This module is reserved for any UseCases that require custom instantiation 
    // (e.g. interfaces, third-party builders, or specific API configurations).
}
