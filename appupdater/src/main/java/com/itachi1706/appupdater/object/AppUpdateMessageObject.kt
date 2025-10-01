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
class AppUpdateMessageObject {
    val index: Int = 0
    val id: String? = null
    val appid: String? = null
    val updateText: String? = null
    val dateModified: String? = null
    val versionCode: String? = null
    val versionName: String? = null
    val labels: String = ""
    val url: String? = null
}
