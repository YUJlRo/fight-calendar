package com.fightcalendar.app.di

import com.fightcalendar.app.data.repository.DayDataRepositoryImpl
import com.fightcalendar.app.domain.repository.DayDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    
    @Binds
    @Singleton
    abstract fun bindDayDataRepository(
        dayDataRepositoryImpl: DayDataRepositoryImpl
    ): DayDataRepository
}