package com.schober.vinylcast

import com.schober.vinylcast.service.MediaRecorderService

abstract class MediaRecorderServiceListener(service: MediaRecorderService,
                                            activity: MainActivity) {
    abstract fun start()

    abstract fun stop()

    abstract fun getRawAudioInputStream(minRawBufferSize: Int)

    abstract fun createThread(sampleRate: Int, bitDepth: Int, channelCount: Int, minRawBufferSize: Int)

    abstract fun startListeningThread()

    abstract fun newData(buffer: ByteArray, offset: Int, bufferReadResult: Int)

    abstract fun interrupt()

    abstract fun close()
}