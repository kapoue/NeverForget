package com.neverforget.domain.usecase

import kotlinx.datetime.LocalDate
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests unitaires pour ValidateTaskFormUseCase
 */
class ValidateTaskFormUseCaseTest {
    
    private lateinit var validateTaskFormUseCase: ValidateTaskFormUseCase
    
    @Before
    fun setup() {
        validateTaskFormUseCase = ValidateTaskFormUseCase()
    }
    
    @Test
    fun `execute should return valid result for correct data`() {
        // Given
        val name = "Détecteurs de fumée"
        val category = "Maison"
        val recurrenceDays = 180
        val nextDueDate = LocalDate(2025, 12, 1)
        
        // When
        val result = validateTaskFormUseCase.execute(name, category, recurrenceDays, nextDueDate)
        
        // Then
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }
    
    @Test
    fun `execute should fail for empty name`() {
        // Given
        val name = ""
        val category = "Maison"
        val recurrenceDays = 30
        val nextDueDate = LocalDate(2025, 12, 1)
        
        // When
        val result = validateTaskFormUseCase.execute(name, category, recurrenceDays, nextDueDate)
        
        // Then
        assertFalse(result.isValid)
        assertTrue(result.hasNameError)
        assertEquals("Le nom de la tâche est obligatoire", result.getError("name"))
    }
    
    @Test
    fun `execute should fail for blank name`() {
        // Given
        val name = "   "
        val category = "Maison"
        val recurrenceDays = 30
        val nextDueDate = LocalDate(2025, 12, 1)
        
        // When
        val result = validateTaskFormUseCase.execute(name, category, recurrenceDays, nextDueDate)
        
        // Then
        assertFalse(result.isValid)
        assertTrue(result.hasNameError)
        assertEquals("Le nom de la tâche est obligatoire", result.getError("name"))
    }
    
    @Test
    fun `execute should fail for too long name`() {
        // Given
        val name = "a".repeat(101) // 101 caractères
        val category = "Maison"
        val recurrenceDays = 30
        val nextDueDate = LocalDate(2025, 12, 1)
        
        // When
        val result = validateTaskFormUseCase.execute(name, category, recurrenceDays, nextDueDate)
        
        // Then
        assertFalse(result.isValid)
        assertTrue(result.hasNameError)
        assertEquals("Le nom ne peut pas dépasser 100 caractères", result.getError("name"))
    }
    
    @Test
    fun `execute should fail for empty category`() {
        // Given
        val name = "Test Task"
        val category = ""
        val recurrenceDays = 30
        val nextDueDate = LocalDate(2025, 12, 1)
        
        // When
        val result = validateTaskFormUseCase.execute(name, category, recurrenceDays, nextDueDate)
        
        // Then
        assertFalse(result.isValid)
        assertTrue(result.hasCategoryError)
        assertEquals("La catégorie est obligatoire", result.getError("category"))
    }
    
    @Test
    fun `execute should fail for zero recurrence`() {
        // Given
        val name = "Test Task"
        val category = "Maison"
        val recurrenceDays = 0
        val nextDueDate = LocalDate(2025, 12, 1)
        
        // When
        val result = validateTaskFormUseCase.execute(name, category, recurrenceDays, nextDueDate)
        
        // Then
        assertFalse(result.isValid)
        assertTrue(result.hasRecurrenceError)
        assertEquals("La récurrence doit être supérieure à 0", result.getError("recurrence"))
    }
    
    @Test
    fun `execute should fail for negative recurrence`() {
        // Given
        val name = "Test Task"
        val category = "Maison"
        val recurrenceDays = -5
        val nextDueDate = LocalDate(2025, 12, 1)
        
        // When
        val result = validateTaskFormUseCase.execute(name, category, recurrenceDays, nextDueDate)
        
        // Then
        assertFalse(result.isValid)
        assertTrue(result.hasRecurrenceError)
        assertEquals("La récurrence doit être supérieure à 0", result.getError("recurrence"))
    }
    
    @Test
    fun `execute should fail for too large recurrence`() {
        // Given
        val name = "Test Task"
        val category = "Maison"
        val recurrenceDays = 3651 // Plus de 10 ans
        val nextDueDate = LocalDate(2025, 12, 1)
        
        // When
        val result = validateTaskFormUseCase.execute(name, category, recurrenceDays, nextDueDate)
        
        // Then
        assertFalse(result.isValid)
        assertTrue(result.hasRecurrenceError)
        assertEquals("La récurrence ne peut pas dépasser 10 ans", result.getError("recurrence"))
    }
    
    @Test
    fun `execute should handle multiple errors`() {
        // Given
        val name = ""
        val category = ""
        val recurrenceDays = 0
        val nextDueDate = LocalDate(2025, 12, 1)
        
        // When
        val result = validateTaskFormUseCase.execute(name, category, recurrenceDays, nextDueDate)
        
        // Then
        assertFalse(result.isValid)
        assertTrue(result.hasNameError)
        assertTrue(result.hasCategoryError)
        assertTrue(result.hasRecurrenceError)
        assertEquals(3, result.errors.size)
    }
    
    @Test
    fun `validateName should return null for valid name`() {
        // Given
        val name = "Détecteurs de fumée"
        
        // When
        val error = validateTaskFormUseCase.validateName(name)
        
        // Then
        assertNull(error)
    }
    
    @Test
    fun `validateRecurrence should return null for valid recurrence`() {
        // Given
        val recurrenceDays = 30
        
        // When
        val error = validateTaskFormUseCase.validateRecurrence(recurrenceDays)
        
        // Then
        assertNull(error)
    }
    
    @Test
    fun `suggestRecurrence should return correct values for categories`() {
        // When & Then
        assertEquals(90, validateTaskFormUseCase.suggestRecurrence("Maison"))
        assertEquals(90, validateTaskFormUseCase.suggestRecurrence("MAISON"))
        assertEquals(30, validateTaskFormUseCase.suggestRecurrence("Voiture"))
        assertEquals(30, validateTaskFormUseCase.suggestRecurrence("Scooter"))
        assertEquals(60, validateTaskFormUseCase.suggestRecurrence("Autre"))
        assertEquals(60, validateTaskFormUseCase.suggestRecurrence("Unknown"))
    }
}