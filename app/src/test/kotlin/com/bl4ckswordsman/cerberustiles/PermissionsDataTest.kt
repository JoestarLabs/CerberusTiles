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
    fun `sync permissions documentation to README`() {
        val candidateLocations = listOf(
            File("../README.md"),
            File("README.md"),
            File("../../README.md")
        )
        val readmeFile = candidateLocations.firstOrNull { it.exists() } ?: return

        val originalText = readmeFile.readText()
        val startMarker = "<!-- PERMISSIONS_START -->"
        val endMarker = "<!-- PERMISSIONS_END -->"

        if (originalText.contains(startMarker) && originalText.contains(endMarker)) {
            val generatedMarkdown = PermissionsData.toMarkdown()
            val pattern = Regex("${Regex.escape(startMarker)}[\\s\\S]*?${Regex.escape(endMarker)}")
            val updatedSection = "$startMarker\n$generatedMarkdown\n$endMarker"
            val updatedText = pattern.replace(originalText, updatedSection)

            if (originalText != updatedText) {
                readmeFile.writeText(updatedText)
            }

            assertTrue(
                "README should contain generated markdown",
                readmeFile.readText().contains(generatedMarkdown)
            )
        }
    }
}
