package com.neverforget.data.repository

import com.neverforget.data.database.SettingsDao
import com.neverforget.data.database.SettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour la gestion des paramètres de l'application
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao
) {
    
    /**
     * Récupère l'heure de notification
     */
    suspend fun getNotificationTime(): String {
        return settingsDao.getSettingValue(SettingsEntity.NOTIFICATION_TIME_KEY)
            ?: SettingsEntity.DEFAULT_NOTIFICATION_TIME
    }
    
    /**
     * Récupère l'heure de notification en Flow
     */
    fun getNotificationTimeFlow(): Flow<String?> {
        return settingsDao.getSettingValueFlow(SettingsEntity.NOTIFICATION_TIME_KEY)
    }
    
    /**
     * Met à jour l'heure de notification
     */
    suspend fun setNotificationTime(time: String) {
        settingsDao.updateSettingValue(SettingsEntity.NOTIFICATION_TIME_KEY, time)
    }
    
    /**
     * Récupère une valeur de paramètre générique
     */
    suspend fun getSettingValue(key: String): String? {
        return settingsDao.getSettingValue(key)
    }
    
    /**
     * Met à jour une valeur de paramètre générique
     */
    suspend fun setSettingValue(key: String, value: String) {
        val setting = SettingsEntity(key = key, value = value)
        settingsDao.insertSetting(setting)
    }
    
    /**
     * Supprime un paramètre
     */
    suspend fun deleteSetting(key: String) {
        settingsDao.deleteSettingByKey(key)
    }
    
    /**
     * Récupère le délai de rappel en jours (valeur directe)
     */
    suspend fun getReminderDelayDays(): Int {
        val value = getSettingValue("reminder_delay_days")
        return value?.toIntOrNull() ?: 3 // 3 jours par défaut
    }
    
    /**
     * Met à jour le délai de rappel en jours
     */
    suspend fun setReminderDelayDays(days: Int) {
        setSettingValue("reminder_delay_days", days.toString())
    }
    
    /**
     * Récupère le délai de rappel en jours (Flow)
     */
    fun getReminderDelayDaysFlow(): Flow<Int> {
        return settingsDao.getSettingValueFlow("reminder_delay_days")
            .map { it?.toIntOrNull() ?: 3 }
    }
}