package com.neverforget.data.database

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate

/**
 * Convertisseurs de types pour Room Database
 */
class Converters {
    
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }
    
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }
    
    @TypeConverter
    fun fromLong(value: Long?): LocalDate? {
        return value?.let { LocalDate.fromEpochDays(it.toInt()) }
    }
    
    @TypeConverter
    fun toLong(date: LocalDate?): Long? {
        return date?.toEpochDays()?.toLong()
    }
}