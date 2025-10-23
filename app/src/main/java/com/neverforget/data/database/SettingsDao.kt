package com.neverforget.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour les opérations sur les paramètres
 */
@Dao
interface SettingsDao {
    
    @Query("SELECT * FROM settings")
    fun getAllSettings(): Flow<List<SettingsEntity>>
    
    @Query("SELECT value FROM settings WHERE key = :key")
    suspend fun getSettingValue(key: String): String?
    
    @Query("SELECT value FROM settings WHERE key = :key")
    fun getSettingValueFlow(key: String): Flow<String?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: SettingsEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: List<SettingsEntity>)
    
    @Update
    suspend fun updateSetting(setting: SettingsEntity)
    
    @Query("UPDATE settings SET value = :value WHERE key = :key")
    suspend fun updateSettingValue(key: String, value: String)
    
    @Delete
    suspend fun deleteSetting(setting: SettingsEntity)
    
    @Query("DELETE FROM settings WHERE key = :key")
    suspend fun deleteSettingByKey(key: String)
}