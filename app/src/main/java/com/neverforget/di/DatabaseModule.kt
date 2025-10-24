package com.neverforget.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.neverforget.data.database.*
import com.neverforget.notifications.NotificationScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Module Hilt pour l'injection de dépendances de la base de données
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }
    
    @Provides
    @Singleton
    fun provideNeverForgetDatabase(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope
    ): NeverForgetDatabase {
        return NeverForgetDatabase.getDatabase(context, scope)
    }
    
    @Provides
    fun provideTaskDao(database: NeverForgetDatabase): TaskDao {
        return database.taskDao()
    }
    
    @Provides
    fun provideTaskHistoryDao(database: NeverForgetDatabase): TaskHistoryDao {
        return database.taskHistoryDao()
    }
    
    @Provides
    fun provideCategoryDao(database: NeverForgetDatabase): CategoryDao {
        return database.categoryDao()
    }
    
    @Provides
    fun provideSettingsDao(database: NeverForgetDatabase): SettingsDao {
        return database.settingsDao()
    }
    
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideNotificationScheduler(
        workManager: WorkManager,
        settingsRepository: com.neverforget.data.repository.SettingsRepository
    ): NotificationScheduler {
        return NotificationScheduler(workManager, settingsRepository)
    }
}

/**
 * Qualifier pour le scope de l'application
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope