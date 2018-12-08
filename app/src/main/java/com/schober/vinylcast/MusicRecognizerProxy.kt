package com.schober.vinylcast

import android.os.Process
import android.util.Log
import com.gracenote.gnsdk.GnException
import com.gracenote.gnsdk.GnMusicIdStream
import com.schober.vinylcast.service.MediaRecorderService
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.*

class MusicRecognizerProxy(service: MediaRecorderService, activity: MainActivity) :
        MediaRecorderServiceListener(service, activity) {

    private val musicRecognizer = MusicRecognizer(service, activity)

    private var musicDetectInputStream: PipedInputStream? = null
    private var musicDetectOutputStream: PipedOutputStream? = null

    private var musicDetectThread: Thread? = null
    private val musicRecognizerTimer = Timer()
    private val MUSIC_RECOGNIZE_INTERVAL = 10000

    override fun start() {
        musicRecognizerTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                musicRecognizer.start()
            }
        }, 0, MUSIC_RECOGNIZE_INTERVAL.toLong())
    }

    override fun stop() {
        musicRecognizer.stop()
        musicRecognizerTimer.cancel()
    }

    override fun getRawAudioInputStream(minRawBufferSize: Int) {
        musicDetectInputStream = PipedInputStream(minRawBufferSize)
        musicDetectOutputStream = PipedOutputStream(musicDetectInputStream)
    }

    override fun createThread(sampleRate: Int, bitDepth: Int, channelCount: Int, minRawBufferSize: Int) {
        musicDetectThread = Thread(MusicDetectRunnable(musicRecognizer.gnMusicIdStream, sampleRate,
                bitDepth, channelCount, minRawBufferSize), "MusicDetect")
    }

    override fun startListeningThread() {
        musicDetectThread!!.start()
    }

    override fun newData(buffer: ByteArray, offset: Int, bufferReadResult: Int) {
        musicDetectOutputStream!!.write(buffer, offset, bufferReadResult)
        musicDetectOutputStream!!.flush()
    }

    override fun interrupt() {
        if (musicDetectThread != null) {
            musicDetectThread!!.interrupt()
        }
    }

    override fun close() {
        musicDetectOutputStream!!.close()
    }

    internal inner class MusicDetectRunnable(
            private val musicIdStream: GnMusicIdStream,
            sampleRate: Int,
            bitDepth: Int,
            channelCount: Int,
            private val minRawBufferSize: Int) : Runnable {

        private val TAG = "MusicDetectRunnable"

        init {
            try {
                musicIdStream.audioProcessStart(sampleRate.toLong(), bitDepth.toLong(),
                        channelCount.toLong())
            } catch (e: GnException) {
                Log.e(TAG, "Exception starting gracenote id", e)
            }

        }

        override fun run() {
            Log.d(TAG, "starting...")
            Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND)

            val buffer = ByteArray(minRawBufferSize)
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val bufferReadResult = musicDetectInputStream!!.read(buffer, 0, buffer.size)
                    musicIdStream!!.audioProcess(buffer, bufferReadResult.toLong())
                } catch (e: GnException) {
                    Log.e(TAG, "Exception writing music detect output", e)
                    break
                } catch (e: IOException) {
                    Log.e(TAG, "Exception writing music detect output", e)
                    break
                }

            }

            Log.d(TAG, "stopping...")
            try {
                musicIdStream!!.audioProcessStop()
            } catch (e: GnException) {
                Log.e(TAG, "Exception closing streams", e)
            }

        }
    }
}