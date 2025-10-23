package com.neverforget.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room représentant les paramètres de l'application
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey 
    val key: String,
    val value: String
) {
    companion object {
        const val NOTIFICATION_TIME_KEY = "notification_time"
        const val DEFAULT_NOTIFICATION_TIME = "11:00"
    }
}