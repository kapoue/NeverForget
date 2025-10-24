package com.neverforget.utils

import android.content.Context
import com.neverforget.R
import com.neverforget.data.model.Category
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests unitaires pour CategoryHelper
 */
class CategoryHelperTest {

    @Test
    fun `getCategoryIcon returns correct drawable resource`() {
        assertEquals(R.drawable.ic_home, CategoryHelper.getCategoryIcon(Category.MAISON))
        assertEquals(R.drawable.ic_car, CategoryHelper.getCategoryIcon(Category.VOITURE))
        assertEquals(R.drawable.ic_scooter, CategoryHelper.getCategoryIcon(Category.SCOOTER))
        assertEquals(R.drawable.ic_other, CategoryHelper.getCategoryIcon(Category.AUTRE))
    }

    @Test
    fun `getCategoryStringRes returns correct string resource`() {
        assertEquals(R.string.category_home, CategoryHelper.getCategoryStringRes(Category.MAISON))
        assertEquals(R.string.category_car, CategoryHelper.getCategoryStringRes(Category.VOITURE))
        assertEquals(R.string.category_scooter, CategoryHelper.getCategoryStringRes(Category.SCOOTER))
        assertEquals(R.string.category_other, CategoryHelper.getCategoryStringRes(Category.AUTRE))
    }

    @Test
    fun `getCategoryDisplayName returns correct hardcoded names`() {
        assertEquals("Maison", CategoryHelper.getCategoryDisplayName(Category.MAISON))
        assertEquals("Voiture", CategoryHelper.getCategoryDisplayName(Category.VOITURE))
        assertEquals("Scooter", CategoryHelper.getCategoryDisplayName(Category.SCOOTER))
        assertEquals("Autre", CategoryHelper.getCategoryDisplayName(Category.AUTRE))
    }

    @Test
    fun `getCategoryDisplayName with context returns localized names`() {
        val mockContext = mockk<Context>()
        every { mockContext.getString(R.string.category_home) } returns "Maison"
        every { mockContext.getString(R.string.category_car) } returns "Voiture"
        every { mockContext.getString(R.string.category_scooter) } returns "Scooter"
        every { mockContext.getString(R.string.category_other) } returns "Autre"

        assertEquals("Maison", CategoryHelper.getCategoryDisplayName(Category.MAISON, mockContext))
        assertEquals("Voiture", CategoryHelper.getCategoryDisplayName(Category.VOITURE, mockContext))
        assertEquals("Scooter", CategoryHelper.getCategoryDisplayName(Category.SCOOTER, mockContext))
        assertEquals("Autre", CategoryHelper.getCategoryDisplayName(Category.AUTRE, mockContext))
    }

    @Test
    fun `getAllCategories returns all categories`() {
        val categories = CategoryHelper.getAllCategories()
        assertEquals(4, categories.size)
        assertTrue(categories.contains(Category.MAISON))
        assertTrue(categories.contains(Category.VOITURE))
        assertTrue(categories.contains(Category.SCOOTER))
        assertTrue(categories.contains(Category.AUTRE))
    }

    @Test
    fun `parseCategory returns correct category for valid strings`() {
        assertEquals(Category.MAISON, CategoryHelper.parseCategory("MAISON"))
        assertEquals(Category.VOITURE, CategoryHelper.parseCategory("VOITURE"))
        assertEquals(Category.SCOOTER, CategoryHelper.parseCategory("SCOOTER"))
        assertEquals(Category.AUTRE, CategoryHelper.parseCategory("AUTRE"))
        assertEquals(Category.MAISON, CategoryHelper.parseCategory("maison"))
    }

    @Test
    fun `parseCategory returns AUTRE for invalid strings`() {
        assertEquals(Category.AUTRE, CategoryHelper.parseCategory("INVALID"))
        assertEquals(Category.AUTRE, CategoryHelper.parseCategory(""))
        assertEquals(Category.AUTRE, CategoryHelper.parseCategory(null))
        assertEquals(Category.AUTRE, CategoryHelper.parseCategory("123"))
    }
}