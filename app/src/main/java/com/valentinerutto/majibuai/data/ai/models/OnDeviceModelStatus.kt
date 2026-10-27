package com.valentinerutto.majibuai.data.ai.models

import java.io.File

sealed interface OnDeviceModelStatus {
    data object Missing : OnDeviceModelStatus
    data class Downloading(
        val downloadedBytes: Long,
        val totalBytes: Long?,
    ) : OnDeviceModelStatus
    data class Ready(val file: File) : OnDeviceModelStatus
    data class Failed(val reason: String) : OnDeviceModelStatus

}
enum class OnDeviceAvailability {
    CHECKING,
    UNAVAILABLE,
    DOWNLOADABLE,
    DOWNLOADING,
    DOWNLOADED,
    INITIALIZING,
    AVAILABLE,
    DOWNLOAD_FAILED
}

data class OnDeviceModelState(
    val availability: OnDeviceAvailability = OnDeviceAvailability.CHECKING,
    val bytesDownloaded: Long = 0L,
    val bytesToDownload: Long? = null,
    val errorMessage: String? = null
)