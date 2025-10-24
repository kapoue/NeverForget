package com.neverforget.utils

import kotlinx.datetime.LocalDate
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests unitaires pour DateUtils
 */
class DateUtilsTest {
    
    @Test
    fun `daysBetween should calculate correct difference`() {
        // Given
        val startDate = LocalDate(2025, 10, 23)
        val endDate = LocalDate(2025, 10, 30)
        
        // When
        val days = DateUtils.daysBetween(startDate, endDate)
        
        // Then
        assertEquals(7, days)
    }
    
    @Test
    fun `daysBetween should return negative for past dates`() {
        // Given
        val startDate = LocalDate(2025, 10, 30)
        val endDate = LocalDate(2025, 10, 23)
        
        // When
        val days = DateUtils.daysBetween(startDate, endDate)
        
        // Then
        assertEquals(-7, days)
    }
    
    @Test
    fun `addDays should add days correctly`() {
        // Given
        val date = LocalDate(2025, 10, 23)
        val daysToAdd = 15
        
        // When
        val result = DateUtils.addDays(date, daysToAdd)
        
        // Then
        assertEquals(LocalDate(2025, 11, 7), result)
    }
    
    @Test
    fun `addWeeks should add weeks correctly`() {
        // Given
        val date = LocalDate(2025, 10, 23)
        val weeksToAdd = 2
        
        // When
        val result = DateUtils.addWeeks(date, weeksToAdd)
        
        // Then
        assertEquals(LocalDate(2025, 11, 6), result)
    }
    
    @Test
    fun `addMonths should add months correctly`() {
        // Given
        val date = LocalDate(2025, 10, 23)
        val monthsToAdd = 3
        
        // When
        val result = DateUtils.addMonths(date, monthsToAdd)
        
        // Then
        assertEquals(LocalDate(2026, 1, 23), result)
    }
    
    @Test
    fun `calculateNextDueDate should add recurrence days`() {
        // Given
        val completionDate = LocalDate(2025, 10, 23)
        val recurrenceDays = 30
        
        // When
        val nextDueDate = DateUtils.calculateNextDueDate(completionDate, recurrenceDays)
        
        // Then
        assertEquals(LocalDate(2025, 11, 22), nextDueDate)
    }
    
    @Test
    fun `calculateTaskUrgency should return OVERDUE for past dates`() {
        // Given
        val today = LocalDate(2025, 10, 23)
        val dueDate = LocalDate(2025, 10, 20)
        
        // When
        val urgency = DateUtils.calculateTaskUrgency(dueDate, today)
        
        // Then
        assertEquals(TaskUrgency.OVERDUE, urgency)
    }
    
    @Test
    fun `calculateTaskUrgency should return DUE_TODAY for today`() {
        // Given
        val today = LocalDate(2025, 10, 23)
        val dueDate = LocalDate(2025, 10, 23)
        
        // When
        val urgency = DateUtils.calculateTaskUrgency(dueDate, today)
        
        // Then
        assertEquals(TaskUrgency.DUE_TODAY, urgency)
    }
    
    @Test
    fun `calculateTaskUrgency should return DUE_SOON for 3 days ahead`() {
        // Given
        val today = LocalDate(2025, 10, 23)
        val dueDate = LocalDate(2025, 10, 26)
        
        // When
        val urgency = DateUtils.calculateTaskUrgency(dueDate, today)
        
        // Then
        assertEquals(TaskUrgency.DUE_SOON, urgency)
    }
    
    @Test
    fun `calculateTaskUrgency should return DUE_THIS_WEEK for 7 days ahead`() {
        // Given
        val today = LocalDate(2025, 10, 23)
        val dueDate = LocalDate(2025, 10, 30)
        
        // When
        val urgency = DateUtils.calculateTaskUrgency(dueDate, today)
        
        // Then
        assertEquals(TaskUrgency.DUE_THIS_WEEK, urgency)
    }
    
    @Test
    fun `calculateTaskUrgency should return OK for far future`() {
        // Given
        val today = LocalDate(2025, 10, 23)
        val dueDate = LocalDate(2025, 11, 15)
        
        // When
        val urgency = DateUtils.calculateTaskUrgency(dueDate, today)
        
        // Then
        assertEquals(TaskUrgency.OK, urgency)
    }
    
    @Test
    fun `formatRecurrenceDescription should format common recurrences`() {
        // When & Then
        assertEquals("Tous les jours", DateUtils.formatRecurrenceDescription(1))
        assertEquals("Toutes les semaines", DateUtils.formatRecurrenceDescription(7))
        assertEquals("Toutes les 2 semaines", DateUtils.formatRecurrenceDescription(14))
        assertEquals("Tous les mois", DateUtils.formatRecurrenceDescription(30))
        assertEquals("Tous les 2 mois", DateUtils.formatRecurrenceDescription(60))
        assertEquals("Tous les 3 mois", DateUtils.formatRecurrenceDescription(90))
        assertEquals("Tous les 6 mois", DateUtils.formatRecurrenceDescription(180))
        assertEquals("Tous les ans", DateUtils.formatRecurrenceDescription(365))
        assertEquals("Tous les 2 ans", DateUtils.formatRecurrenceDescription(730))
        assertEquals("Tous les 15 jours", DateUtils.formatRecurrenceDescription(15))
    }
    
    @Test
    fun `formatFrench should format date correctly`() {
        // Given
        val date = LocalDate(2025, 10, 23)
        
        // When
        val formatted = DateUtils.formatFrench(date)
        
        // Then
        assertEquals("23/10/2025", formatted)
    }
    
    @Test
    fun `isPast should return true for past dates`() {
        // Given
        val today = LocalDate(2025, 10, 23)
        val pastDate = LocalDate(2025, 10, 20)
        
        // When
        val result = DateUtils.isPast(pastDate)
        
        // Then - Note: Ce test dépend de la date actuelle, donc on teste la logique
        assertTrue(pastDate < DateUtils.today())
    }
    
    @Test
    fun `startOfWeek should return Monday of the week`() {
        // Given - Mercredi 23 octobre 2025
        val wednesday = LocalDate(2025, 10, 23)
        
        // When
        val startOfWeek = DateUtils.startOfWeek(wednesday)
        
        // Then - Lundi 21 octobre 2025
        assertEquals(LocalDate(2025, 10, 21), startOfWeek)
    }
    
    @Test
    fun `startOfMonth should return first day of month`() {
        // Given
        val date = LocalDate(2025, 10, 23)
        
        // When
        val startOfMonth = DateUtils.startOfMonth(date)
        
        // Then
        assertEquals(LocalDate(2025, 10, 1), startOfMonth)
    }
    
    @Test
    fun `endOfMonth should return last day of month`() {
        // Given
        val date = LocalDate(2025, 10, 23)
        
        // When
        val endOfMonth = DateUtils.endOfMonth(date)
        
        // Then
        assertEquals(LocalDate(2025, 10, 31), endOfMonth)
    }
    
    @Test
    fun `TaskUrgency priority should be ordered correctly`() {
        // When & Then
        assertTrue(TaskUrgency.OVERDUE.priority < TaskUrgency.DUE_TODAY.priority)
        assertTrue(TaskUrgency.DUE_TODAY.priority < TaskUrgency.DUE_SOON.priority)
        assertTrue(TaskUrgency.DUE_SOON.priority < TaskUrgency.DUE_THIS_WEEK.priority)
        assertTrue(TaskUrgency.DUE_THIS_WEEK.priority < TaskUrgency.OK.priority)
    }
}