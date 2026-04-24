package com.itachi1706.appupdater.`object`

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UpdateShellTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun defaultValues_areCorrect() {
        val shell = UpdateShell()
        assertNull(shell.msg)
        assertEquals(0, shell.error)
    }

    @Test
    fun deserialization_withError_populatesErrorCode() {
        val jsonString = """{"error":20}"""
        val shell = json.decodeFromString<UpdateShell>(jsonString)
        assertEquals(20, shell.error)
        assertNull(shell.msg)
    }

    @Test
    fun deserialization_withMsgObject_populatesMsg() {
        val jsonString = """{"error":0,"msg":{"latestVersion":"1.0","latestVersionCode":"100","updateMessage":[]}}"""
        val shell = json.decodeFromString<UpdateShell>(jsonString)
        assertEquals(0, shell.error)
        assertEquals("1.0", shell.msg?.latestVersion)
        assertEquals("100", shell.msg?.latestVersionCode)
    }

    @Test
    fun deserialization_withNullMsg_msgIsNull() {
        val jsonString = """{"error":0,"msg":null}"""
        val shell = json.decodeFromString<UpdateShell>(jsonString)
        assertNull(shell.msg)
    }

    @Test
    fun deserialization_ignoresUnknownKeys() {
        val jsonString = """{"error":0,"unknownField":"value"}"""
        val shell = json.decodeFromString<UpdateShell>(jsonString)
        assertEquals(0, shell.error)
    }

    @Test
    fun serialization_roundTrip_preservesData() {
        val msg = AppUpdateObject(latestVersion = "2.0", latestVersionCode = "200")
        val shell = UpdateShell(msg = msg, error = 0)
        val serialized = json.encodeToString(shell)
        val deserialized = json.decodeFromString<UpdateShell>(serialized)
        assertEquals(shell.error, deserialized.error)
        assertEquals(shell.msg, deserialized.msg)
    }
}

