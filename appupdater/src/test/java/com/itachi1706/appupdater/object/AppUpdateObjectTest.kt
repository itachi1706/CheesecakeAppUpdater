package com.itachi1706.appupdater.`object`

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateObjectTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun defaultValues_areCorrect() {
        val obj = AppUpdateObject()
        assertEquals(0, obj.index)
        assertNull(obj.id)
        assertNull(obj.packageName)
        assertNull(obj.appName)
        assertNull(obj.latestVersion)
        assertNull(obj.latestVersionCode)
        assertTrue(obj.updateMessage.isEmpty())
    }

    @Test
    fun deserialization_populatesFields() {
        val jsonString = """{"index":1,"id":"abc","packageName":"com.example","appName":"Test App","latestVersion":"2.0","latestVersionCode":"200","updateMessage":[]}"""
        val obj = json.decodeFromString<AppUpdateObject>(jsonString)
        assertEquals(1, obj.index)
        assertEquals("abc", obj.id)
        assertEquals("com.example", obj.packageName)
        assertEquals("Test App", obj.appName)
        assertEquals("2.0", obj.latestVersion)
        assertEquals("200", obj.latestVersionCode)
    }

    @Test
    fun deserialization_withUpdateMessages_populatesArray() {
        val jsonString = """{"updateMessage":[{"versionName":"1.0","updateText":"Hello"}]}"""
        val obj = json.decodeFromString<AppUpdateObject>(jsonString)
        assertEquals(1, obj.updateMessage.size)
        assertEquals("1.0", obj.updateMessage[0].versionName)
        assertEquals("Hello", obj.updateMessage[0].updateText)
    }

    @Test
    fun deserialization_ignoresUnknownKeys() {
        val jsonString = """{"unknownField":"value","latestVersion":"1.0","updateMessage":[]}"""
        val obj = json.decodeFromString<AppUpdateObject>(jsonString)
        assertEquals("1.0", obj.latestVersion)
    }

    @Test
    fun equality_sameData_areEqual() {
        val obj1 = AppUpdateObject(index = 1, id = "test", latestVersion = "1.0")
        val obj2 = AppUpdateObject(index = 1, id = "test", latestVersion = "1.0")
        assertEquals(obj1, obj2)
    }

    @Test
    fun equality_differentData_areNotEqual() {
        val obj1 = AppUpdateObject(index = 1, latestVersion = "1.0")
        val obj2 = AppUpdateObject(index = 1, latestVersion = "2.0")
        assertTrue(obj1 != obj2)
    }

    @Test
    fun hashCode_equalObjects_haveEqualHashCodes() {
        val obj1 = AppUpdateObject(index = 5, id = "x", latestVersionCode = "50")
        val obj2 = AppUpdateObject(index = 5, id = "x", latestVersionCode = "50")
        assertEquals(obj1.hashCode(), obj2.hashCode())
    }

    @Test
    fun serialization_roundTrip_preservesData() {
        val obj = AppUpdateObject(
            index = 3,
            id = "id123",
            packageName = "com.test",
            latestVersion = "3.0",
            latestVersionCode = "300",
            updateMessage = arrayOf(AppUpdateMessageObject(versionName = "3.0", updateText = "Changes"))
        )
        val serialized = json.encodeToString(obj)
        val deserialized = json.decodeFromString<AppUpdateObject>(serialized)
        assertEquals(obj, deserialized)
    }
}

