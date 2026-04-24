package com.itachi1706.appupdater.`object`

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppUpdateMessageObjectTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun defaultValues_areCorrect() {
        val obj = AppUpdateMessageObject()
        assertEquals(0, obj.index)
        assertNull(obj.id)
        assertNull(obj.appid)
        assertNull(obj.updateText)
        assertNull(obj.versionCode)
        assertNull(obj.versionName)
        assertEquals("", obj.labels)
        assertNull(obj.url)
    }

    @Test
    fun deserialization_populatesAllFields() {
        val jsonString = """{"index":2,"id":"msg1","appid":"app1","updateText":"Fixed bugs","versionCode":"100","versionName":"1.0","labels":"[Fix]","url":"https://example.com/update.apk"}"""
        val obj = json.decodeFromString<AppUpdateMessageObject>(jsonString)
        assertEquals(2, obj.index)
        assertEquals("msg1", obj.id)
        assertEquals("app1", obj.appid)
        assertEquals("Fixed bugs", obj.updateText)
        assertEquals("100", obj.versionCode)
        assertEquals("1.0", obj.versionName)
        assertEquals("[Fix]", obj.labels)
        assertEquals("https://example.com/update.apk", obj.url)
    }

    @Test
    fun deserialization_missingOptionalFields_usesDefaults() {
        val jsonString = """{"versionName":"1.5"}"""
        val obj = json.decodeFromString<AppUpdateMessageObject>(jsonString)
        assertEquals("1.5", obj.versionName)
        assertEquals("", obj.labels)
        assertNull(obj.updateText)
    }

    @Test
    fun serialization_roundTrip_preservesData() {
        val obj = AppUpdateMessageObject(
            index = 1, id = "id1", updateText = "Some change",
            versionName = "2.0", labels = "[New]", url = "https://example.com/app.apk"
        )
        val serialized = json.encodeToString(obj)
        val deserialized = json.decodeFromString<AppUpdateMessageObject>(serialized)
        assertEquals(obj, deserialized)
    }
}

