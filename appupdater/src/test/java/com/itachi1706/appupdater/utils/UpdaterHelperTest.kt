package com.itachi1706.appupdater.utils

import com.itachi1706.appupdater.`object`.AppUpdateMessageObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdaterHelperTest {

    @Test
    fun changelogStringFromEmptyArray_returnsEmptyString() {
        val result = UpdaterHelper.getChangelogStringFromArray(emptyArray())
        assertEquals("", result)
    }

    @Test
    fun changelogStringFromSingleEntry_containsVersionName() {
        val obj = AppUpdateMessageObject(versionName = "1.0.0", updateText = "Initial release", labels = "")
        val result = UpdaterHelper.getChangelogStringFromArray(arrayOf(obj))
        assertTrue(result.contains("1.0.0"))
    }

    @Test
    fun changelogStringFromSingleEntry_containsUpdateText() {
        val obj = AppUpdateMessageObject(versionName = "1.0.0", updateText = "Initial release", labels = "")
        val result = UpdaterHelper.getChangelogStringFromArray(arrayOf(obj))
        assertTrue(result.contains("Initial release"))
    }

    @Test
    fun changelogStringFromSingleEntry_containsLabels() {
        val obj = AppUpdateMessageObject(versionName = "2.0.0", updateText = "Big update", labels = "[Beta]")
        val result = UpdaterHelper.getChangelogStringFromArray(arrayOf(obj))
        assertTrue(result.contains("[Beta]"))
    }

    @Test
    fun changelogStringFromSingleEntry_containsBoldTag() {
        val obj = AppUpdateMessageObject(versionName = "1.0.0", updateText = "some text", labels = "")
        val result = UpdaterHelper.getChangelogStringFromArray(arrayOf(obj))
        assertTrue(result.contains("<b>"))
        assertTrue(result.contains("</b>"))
    }

    @Test
    fun changelogStringFromMultipleEntries_containsAllVersionNames() {
        val obj1 = AppUpdateMessageObject(versionName = "1.0.0", updateText = "First", labels = "")
        val obj2 = AppUpdateMessageObject(versionName = "2.0.0", updateText = "Second", labels = "")
        val result = UpdaterHelper.getChangelogStringFromArray(arrayOf(obj1, obj2))
        assertTrue(result.contains("1.0.0"))
        assertTrue(result.contains("2.0.0"))
    }

    @Test
    fun changelogStringFromMultipleEntries_containsAllUpdateTexts() {
        val obj1 = AppUpdateMessageObject(versionName = "1.0.0", updateText = "First release", labels = "")
        val obj2 = AppUpdateMessageObject(versionName = "2.0.0", updateText = "Second release", labels = "")
        val result = UpdaterHelper.getChangelogStringFromArray(arrayOf(obj1, obj2))
        assertTrue(result.contains("First release"))
        assertTrue(result.contains("Second release"))
    }

    @Test
    fun changelogStringWithUnixNewlines_replacesWithHtmlBreaks() {
        val obj = AppUpdateMessageObject(versionName = "1.0.0", updateText = "Line1\nLine2", labels = "")
        val result = UpdaterHelper.getChangelogStringFromArray(arrayOf(obj))
        assertTrue(result.contains("<br/>"))
        assertTrue(!result.contains("\n"))
    }

    @Test
    fun changelogStringWithWindowsNewlines_replacesWithHtmlBreaks() {
        val obj = AppUpdateMessageObject(versionName = "1.0.0", updateText = "Line1\r\nLine2", labels = "")
        val result = UpdaterHelper.getChangelogStringFromArray(arrayOf(obj))
        assertTrue(result.contains("<br/>"))
        assertTrue(!result.contains("\r\n"))
    }

    @Test
    fun changelogStringWithCarriageReturns_replacesWithHtmlBreaks() {
        val obj = AppUpdateMessageObject(versionName = "1.0.0", updateText = "Line1\rLine2", labels = "")
        val result = UpdaterHelper.getChangelogStringFromArray(arrayOf(obj))
        assertTrue(result.contains("<br/>"))
        assertTrue(!result.contains("\r"))
    }

    @Test
    fun changelogStringWithNullUpdateText_doesNotThrow() {
        val obj = AppUpdateMessageObject(versionName = "1.0.0", updateText = null, labels = "")
        val result = UpdaterHelper.getChangelogStringFromArray(arrayOf(obj))
        assertTrue(result.contains("1.0.0"))
    }

    @Test
    fun updaterHelperHttpQueryTimeoutConstant_is15Seconds() {
        assertEquals(15000, UpdaterHelper.HTTP_QUERY_TIMEOUT)
    }

    @Test
    fun updaterHelperNotificationChannelConstant_hasExpectedValue() {
        assertEquals("app_update_channel", UpdaterHelper.UPDATER_NOTIFICATION_CHANNEL)
    }
}

