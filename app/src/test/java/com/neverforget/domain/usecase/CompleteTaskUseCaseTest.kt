package com.neverforget.domain.usecase

import com.neverforget.data.model.Task
import com.neverforget.data.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests unitaires pour CompleteTaskUseCase
 */
class CompleteTaskUseCaseTest {
    
    private lateinit var taskRepository: TaskRepository
    private lateinit var completeTaskUseCase: CompleteTaskUseCase
    
    @Before
    fun setup() {
        taskRepository = mockk()
        completeTaskUseCase = CompleteTaskUseCase(taskRepository)
    }
    
    @Test
    fun `execute should complete task and calculate next due date`() = runTest {
        // Given
        val taskId = "test-task-id"
        val completionDate = LocalDate(2025, 10, 23)
        val task = Task(
            id = taskId,
            name = "Test Task",
            category = "Maison",
            recurrenceDays = 30,
            nextDueDate = LocalDate(2025, 10, 23),
            createdAt = LocalDate(2025, 10, 1)
        )
        val expectedNextDueDate = LocalDate(2025, 11, 22) // 30 jours après
        
        coEvery { taskRepository.getTaskById(taskId) } returns task
        coEvery { taskRepository.updateTask(any()) } returns Unit
        coEvery { taskRepository.addTaskHistory(any(), any()) } returns Unit
        
        // When
        val result = completeTaskUseCase.execute(taskId, completionDate)
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify {
            taskRepository.updateTask(
                task.copy(nextDueDate = expectedNextDueDate)
            )
        }
        coVerify {
            taskRepository.addTaskHistory(taskId, completionDate)
        }
    }
    
    @Test
    fun `execute should fail when task not found`() = runTest {
        // Given
        val taskId = "non-existent-task"
        val completionDate = LocalDate(2025, 10, 23)
        
        coEvery { taskRepository.getTaskById(taskId) } returns null
        
        // When
        val result = completeTaskUseCase.execute(taskId, completionDate)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Tâche non trouvée", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `execute should use current date when no date provided`() = runTest {
        // Given
        val taskId = "test-task-id"
        val task = Task(
            id = taskId,
            name = "Test Task",
            category = "Maison",
            recurrenceDays = 7,
            nextDueDate = LocalDate(2025, 10, 23),
            createdAt = LocalDate(2025, 10, 1)
        )
        
        coEvery { taskRepository.getTaskById(taskId) } returns task
        coEvery { taskRepository.updateTask(any()) } returns Unit
        coEvery { taskRepository.addTaskHistory(any(), any()) } returns Unit
        
        // When
        val result = completeTaskUseCase.execute(taskId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { taskRepository.addTaskHistory(taskId, any()) }
    }
    
    @Test
    fun `calculateNextDueDate should add recurrence days correctly`() = runTest {
        // Given
        val completionDate = LocalDate(2025, 10, 23)
        val recurrenceDays = 15
        val expectedNextDueDate = LocalDate(2025, 11, 7)
        
        val task = Task(
            id = "test",
            name = "Test",
            category = "Test",
            recurrenceDays = recurrenceDays,
            nextDueDate = completionDate,
            createdAt = LocalDate(2025, 10, 1)
        )
        
        coEvery { taskRepository.getTaskById("test") } returns task
        coEvery { taskRepository.updateTask(any()) } returns Unit
        coEvery { taskRepository.addTaskHistory(any(), any()) } returns Unit
        
        // When
        val result = completeTaskUseCase.execute("test", completionDate)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify {
            taskRepository.updateTask(
                task.copy(nextDueDate = expectedNextDueDate)
            )
        }
    }
}