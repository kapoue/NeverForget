package com.neverforget.di

import com.neverforget.data.repository.TaskRepository
import com.neverforget.domain.usecase.CalculateTaskStatsUseCase
import com.neverforget.domain.usecase.CompleteTaskUseCase
import com.neverforget.domain.usecase.GetTasksWithStatusUseCase
import com.neverforget.domain.usecase.ValidateTaskFormUseCase
import com.neverforget.notifications.NotificationScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Hilt pour l'injection des Use Cases
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    
    @Provides
    @Singleton
    fun provideCompleteTaskUseCase(
        taskRepository: TaskRepository,
        notificationScheduler: NotificationScheduler
    ): CompleteTaskUseCase {
        return CompleteTaskUseCase(taskRepository, notificationScheduler)
    }
    
    @Provides
    @Singleton
    fun provideGetTasksWithStatusUseCase(
        taskRepository: TaskRepository
    ): GetTasksWithStatusUseCase {
        return GetTasksWithStatusUseCase(taskRepository)
    }
    
    @Provides
    @Singleton
    fun provideValidateTaskFormUseCase(): ValidateTaskFormUseCase {
        return ValidateTaskFormUseCase()
    }
    
    @Provides
    @Singleton
    fun provideCalculateTaskStatsUseCase(
        taskRepository: TaskRepository
    ): CalculateTaskStatsUseCase {
        return CalculateTaskStatsUseCase(taskRepository)
    }
}