package com.example.arcoresample

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.core.content.FileProvider
import com.google.ar.sceneform.SceneView
import java.io.File
import java.io.IOException

/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 * Video Recorder class handles recording the contents of a SceneView. It uses MediaRecorder to
 * encode the video. The quality settings can be set explicitly or simply use the CamcorderProfile
 * class to select a predefined set of parameters.
 */
class VideoRecorder(val context: Context) {
    var isRecording = false
        private set
    private var mediaRecorder: MediaRecorder? = null
    private var videoSize: Size? = null
    private var sceneView: SceneView? = null
    private var videoCodec = 0
    private var videoDirectory: File? = null
    private var videoBaseName: String? = null
    var videoPath: File? = null
        private set
    private var encoderSurface: Surface? = null


    companion object {
        private const val TAG = "VideoRecorder"
        private const val DEFAULT_BITRATE = 10000000
        private const val DEFAULT_FRAMERATE = 30
        private  val FALLBACK_QUALITY_LEVELS: IntArray = intArrayOf(
            CamcorderProfile.QUALITY_HIGH,
            CamcorderProfile.QUALITY_2160P,
            CamcorderProfile.QUALITY_1080P,
            CamcorderProfile.QUALITY_720P,
            CamcorderProfile.QUALITY_480P
        )
    }


    fun setSceneView(sceneView: SceneView?) {
        this.sceneView = sceneView
    }


    fun onToggleRecord(): Boolean {
        if (isRecording) {
            stopRecordingVideo()
        } else {
            startRecordingVideo()
        }
        return isRecording
    }

    fun stopRecording() {
      val isRecording=  onToggleRecord()
        if(!isRecording) {
            val videoPathString: String = videoPath!!.getAbsolutePath()
            sendToMediaSource(videoPathString, context)
            openVideo()
        }
    }

    private fun openVideo() {
        val photoURI = FileProvider.getUriForFile(
            context,
            context.getPackageName() + ".provider",
            videoPath!!
        );
        val intent = Intent(Intent.ACTION_VIEW, photoURI);
        intent.setDataAndType(photoURI, "video/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

    private fun sendToMediaSource(videoPath: String, context: Context) {
        val values = ContentValues()
        values.put(MediaStore.Video.Media.TITLE, "Sceneform Video")
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        values.put(MediaStore.Video.Media.DATA, videoPath)
        context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
    }

    fun toggleRecording() {
        if (!hasPermission(context)) return
        onToggleRecord()
    }

    private fun startRecordingVideo() {
        if (mediaRecorder == null) {
            mediaRecorder = MediaRecorder()
        }
        try {
            buildFilename()
            setUpMediaRecorder()
        } catch (e: IOException) {
            Log.e(TAG, "Exception setting up recorder", e)
            return
        }
        // Set up Surface for the MediaRecorder
        encoderSurface = mediaRecorder!!.surface
        sceneView!!.startMirroringToSurface(
            encoderSurface, 0, 0, videoSize!!.width, videoSize!!.height
        )
        isRecording = true
    }

    private fun buildFilename() {
        if (videoDirectory == null) {
            videoDirectory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + "/Sceneform"
            )
        }
        if (videoBaseName == null || videoBaseName!!.isEmpty()) {
            videoBaseName = "Sample"
        }
        videoPath = File(
            videoDirectory,
            videoBaseName + java.lang.Long.toHexString(System.currentTimeMillis()) + ".mp4"
        )
        val dir = videoPath!!.parentFile
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    private fun stopRecordingVideo() { // UI
        isRecording = false
        if (encoderSurface != null) {
            sceneView!!.stopMirroringToSurface(encoderSurface)
            encoderSurface = null
        }
        // Stop recording
        mediaRecorder!!.stop()
        mediaRecorder!!.reset()
    }

    @Throws(IOException::class)
    private fun setUpMediaRecorder() {

        var profile: CamcorderProfile? = null
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_2160P)) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_2160P)
        }
        if (profile == null) { // Select a quality  that is available on this device.
            for (level in VideoRecorder.FALLBACK_QUALITY_LEVELS) {
                if (CamcorderProfile.hasProfile(level)) {
                    profile = CamcorderProfile.get(level)
                    break
                }
            }
        }
        videoSize = Size(profile!!.videoFrameHeight, profile.videoFrameWidth)

        mediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder!!.setOutputFile(videoPath!!.absolutePath)
        mediaRecorder!!.setVideoEncodingBitRate(DEFAULT_BITRATE)
        mediaRecorder!!.setVideoFrameRate(DEFAULT_FRAMERATE)
        mediaRecorder!!.setVideoSize(videoSize!!.width, videoSize!!.height)
        mediaRecorder!!.setVideoEncoder(videoCodec)
        mediaRecorder!!.prepare()
        try {
            mediaRecorder!!.start()
        } catch (e: IllegalStateException) {
            Log.e(
                TAG,
                "Exception starting capture: " + e.message,
                e
            )
        }
    }


}