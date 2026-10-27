package com.valentinerutto.majibuai.data.ai.ondevice

import com.google.ai.edge.litertlm.Engine
import com.valentinerutto.majibuai.data.ai.models.OnDeviceAvailability
import com.valentinerutto.majibuai.data.ai.models.OnDeviceModelState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LiteRTLmEngine (private val modelManager: OnDeviceModelManager){
    private val mutex = Mutex()
    private var engine: Engine? = null

    private val _modelState = MutableStateFlow(OnDeviceModelState())
    val modelState = _modelState.asStateFlow()

    suspend fun refreshModelStatus() = mutex.withLock {
        _modelState.value = OnDeviceModelState(
            availability = when {
                engine != null -> OnDeviceAvailability.AVAILABLE
                modelManager.isDownloaded() -> OnDeviceAvailability.DOWNLOADED
                else -> OnDeviceAvailability.DOWNLOADABLE
            }
        )
    }




}