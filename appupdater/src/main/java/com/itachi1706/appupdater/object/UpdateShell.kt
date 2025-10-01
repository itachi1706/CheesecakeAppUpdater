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
data class UpdateShell {
    val msg: AppUpdateObject? = null
    val error: Int = 0
}
