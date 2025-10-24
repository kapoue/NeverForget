package com.neverforget.notifications

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.neverforget.data.model.Category
import com.neverforget.data.model.Task
import com.neverforget.workers.TaskNotificationWorker
import io.mockk.*
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Tests unitaires pour NotificationScheduler
 */
class NotificationSchedulerTest {

    private lateinit var workManager: WorkManager
    private lateinit var notificationScheduler: NotificationScheduler

    @Before
    fun setup() {
        workManager = mockk(relaxed = true)
        notificationScheduler = NotificationScheduler(workManager)
    }

    @Test
    fun `scheduleNotification creates work request with correct parameters`() {
        // Given
        val task = Task(
            id = "test-id",
            name = "Test Task",
            category = Category.MAISON,
            recurrenceDays = 30,
            nextDueDate = LocalDate(2024, 1, 15),
            createdAt = LocalDate(2024, 1, 1)
        )

        val slot = slot<OneTimeWorkRequest>()
        every { workManager.enqueue(capture(slot)) } returns mockk()

        // When
        notificationScheduler.scheduleNotification(task)

        // Then
        verify { workManager.enqueue(any<OneTimeWorkRequest>()) }
        
        val capturedRequest = slot.captured
        val inputData = capturedRequest.workSpec.input
        
        assert(inputData.getString("task_id") == "test-id")
        assert(inputData.getString("task_name") == "Test Task")
        assert(capturedRequest.workSpec.workerClassName == TaskNotificationWorker::class.java.name)
    }

    @Test
    fun `cancelNotification calls WorkManager cancelUniqueWork`() {
        // Given
        val taskId = "test-task-id"

        // When
        notificationScheduler.cancelNotification(taskId)

        // Then
        verify { workManager.cancelUniqueWork("notification_$taskId") }
    }

    @Test
    fun `scheduleNotification with past due date schedules immediately`() {
        // Given
        val pastDate = LocalDate(2020, 1, 1) // Date dans le passé
        val task = Task(
            id = "test-id",
            name = "Past Task",
            category = Category.VOITURE,
            recurrenceDays = 30,
            nextDueDate = pastDate,
            createdAt = LocalDate(2019, 12, 1)
        )

        val slot = slot<OneTimeWorkRequest>()
        every { workManager.enqueue(capture(slot)) } returns mockk()

        // When
        notificationScheduler.scheduleNotification(task)

        // Then
        verify { workManager.enqueue(any<OneTimeWorkRequest>()) }
        
        val capturedRequest = slot.captured
        // Vérifier que le délai est minimal (0 ou très petit)
        assert(capturedRequest.workSpec.initialDelay <= TimeUnit.MINUTES.toMillis(1))
    }

    @Test
    fun `scheduleNotification creates unique work with correct tag`() {
        // Given
        val task = Task(
            id = "unique-task-id",
            name = "Unique Task",
            category = Category.SCOOTER,
            recurrenceDays = 7,
            nextDueDate = LocalDate(2024, 6, 1),
            createdAt = LocalDate(2024, 5, 1)
        )

        val slot = slot<OneTimeWorkRequest>()
        every { workManager.enqueue(capture(slot)) } returns mockk()

        // When
        notificationScheduler.scheduleNotification(task)

        // Then
        verify { workManager.enqueue(any<OneTimeWorkRequest>()) }
        
        val capturedRequest = slot.captured
        assert(capturedRequest.tags.contains("notification_unique-task-id"))
    }
}