package com.valentinerutto.majibuai.data.ai.ondevice

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class OnDeviceModelManager(context: Context, private val modelUrl: String){
    private val modelFile = File(
        context.filesDir,
        "models/majibu-model.litertlm"
    )
    private val downloadMutex = Mutex()
    private val temporaryFile = File(
        context.filesDir,
        "models/majibu-model.litertlm.tmp"
    )
    fun isDownloaded(): Boolean =
        modelFile.isFile && modelFile.length() > 0L

    fun modelPath(): String = modelFile.absolutePath

    suspend fun requireModelFile(): File = withContext(Dispatchers.IO){
        check(isDownloaded()) { "Model is not downloaded" }
        modelFile
    }

    suspend fun downloadModel( onProgress: (downloaded: Long, total: Long?) -> Unit
    ) = downloadMutex.withLock {
        withContext(Dispatchers.IO) {

            if (isDownloaded()) return@withContext

            modelFile.parentFile?.let { directory ->
                if (!directory.isDirectory && !directory.mkdirs()){
                    throw IOException("Failed to create model directory: $directory")
                }

            }
            temporaryFile.delete()

            val connection = URL(modelUrl).openConnection() as HttpURLConnection

            try {
                connection.connectTimeout = 30_000
                connection.readTimeout = 60_000
                connection.instanceFollowRedirects = true
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw IOException(
                        "Model download failed: HTTP ${connection.responseCode}"
                    )
                }
                val totalBytes = connection.contentLengthLong
                    .takeIf { it > 0L }

                var downloadedBytes = 0L

                connection.inputStream.use { input ->
                    temporaryFile.outputStream().buffered().use { output ->
                        val buffer = ByteArray(64 * 1024)

                        while (true) {
                            coroutineContext.ensureActive()

                            val count = input.read(buffer)
                            if (count == -1) break

                            output.write(buffer, 0, count)
                            downloadedBytes += count

                            onProgress(downloadedBytes, totalBytes)
                        }
                    }
                }
                if (downloadedBytes == 0L) {
                    throw IOException("Downloaded model is empty.")
                }

                if (totalBytes != null && downloadedBytes != totalBytes) {
                    throw IOException("Incomplete model download.")
                }

                if (!temporaryFile.renameTo(modelFile)) {
                    throw IOException("Could not save the downloaded model.")
                }
            } finally{
                connection.disconnect()
                temporaryFile.delete()
            }


        }
    }

}