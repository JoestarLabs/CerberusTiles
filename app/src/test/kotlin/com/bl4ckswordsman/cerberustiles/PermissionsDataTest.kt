package com.bl4ckswordsman.cerberustiles

import com.bl4ckswordsman.cerberustiles.data.PermissionsData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PermissionsDataTest {

    @Test
    fun `permissions and faqs are populated`() {
        assertEquals(4, PermissionsData.permissions.size)
        assertEquals(4, PermissionsData.faqs.size)
    }

    @Test
    fun `toMarkdown outputs markdown with table and details tags`() {
        val markdown = PermissionsData.toMarkdown()
        assertTrue(markdown.contains("## Permissions & Privacy"))
        assertTrue(markdown.contains("| Permission | Feature | Why It Is Needed | How It Is Granted |"))
        assertTrue(markdown.contains("<details>"))
        assertTrue(markdown.contains("### Frequently Asked Questions"))
    }

    @Test
    fun `README permissions section matches PermissionsData`() {
        val candidateLocations = listOf(
            File("../README.md"),
            File("README.md"),
            File("../../README.md")
        )
        val readmeFile = candidateLocations.firstOrNull { it.exists() }
            ?: error("README.md not found in expected locations")

        val originalText = readmeFile.readText()
        val startMarker = "<!-- PERMISSIONS_START -->"
        val endMarker = "<!-- PERMISSIONS_END -->"
        val startIdx = originalText.indexOf(startMarker)
        val endIdx = originalText.indexOf(endMarker)
        assertTrue("README.md must contain $startMarker and $endMarker", startIdx >= 0 && endIdx > startIdx)

        val currentSection = originalText.substring(startIdx + startMarker.length, endIdx).trim()
        assertEquals(
            "README.md permissions section is out of sync with PermissionsData.toMarkdown()",
            PermissionsData.toMarkdown().trim(),
            currentSection
        )
    }
}
