package com.itachi1706.appupdater.`object`

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.appupdate.Objects in AppUpdater
 */
@Suppress("unused")
@Keep
@Serializable
data class AppUpdateObject(
    val index: Int = 0, val id: String? = null, val packageName: String? = null,
    val appName: String? = null,
    val dateCreated: String? = null,
    val latestVersion: String? = null,
    val latestVersionCode: String? = null,
    val apptype: String? = null,
    val updateMessage: Array<AppUpdateMessageObject> = arrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppUpdateObject

        if (index != other.index) return false
        if (id != other.id) return false
        if (packageName != other.packageName) return false
        if (appName != other.appName) return false
        if (dateCreated != other.dateCreated) return false
        if (latestVersion != other.latestVersion) return false
        if (latestVersionCode != other.latestVersionCode) return false
        if (apptype != other.apptype) return false
        if (!updateMessage.contentEquals(other.updateMessage)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + (packageName?.hashCode() ?: 0)
        result = 31 * result + (appName?.hashCode() ?: 0)
        result = 31 * result + (dateCreated?.hashCode() ?: 0)
        result = 31 * result + (latestVersion?.hashCode() ?: 0)
        result = 31 * result + (latestVersionCode?.hashCode() ?: 0)
        result = 31 * result + (apptype?.hashCode() ?: 0)
        result = 31 * result + updateMessage.contentHashCode()
        return result
    }
}
