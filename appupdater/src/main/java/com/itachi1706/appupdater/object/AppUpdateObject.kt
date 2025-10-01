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
class AppUpdateObject {
    val index: Int = 0
    val id: String? = null
    val packageName: String? = null
    val appName: String? = null
    val dateCreated: String? = null
    val latestVersion: String? = null
    val latestVersionCode: String? = null
    val apptype: String? = null
    val updateMessage: Array<AppUpdateMessageObject> = arrayOf()
}
