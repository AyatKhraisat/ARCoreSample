package com.example.arcoresample

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.view.PixelCopy
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.ArSceneView
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream


/**
 *Created by Ayat Khriasat on 22,November,2019 at 19:30
 *Email: ayatzkhraisat@gmail.com
 *Project: ARCoreSample
 **/

const val folderName = "arCoreSample/"

public fun saveImage(bitmap: Bitmap, context: Context, fileName: String) {

    if (android.os.Build.VERSION.SDK_INT >= 29) {
        val values = contentValues()
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + folderName)
        values.put(MediaStore.Images.Media.IS_PENDING, true)
        val uri: Uri? =
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
            values.put(MediaStore.Images.Media.IS_PENDING, false)
            context.contentResolver.update(uri, values, null, null)
        }
    } else {
        val directory = File(getDirectory())
        // getExternalStorageDirectory is deprecated in API 29

        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, fileName)
        saveImageToStream(bitmap, FileOutputStream(file))
        val values = contentValues()
        values.put(MediaStore.Images.Media.DATA, file.absolutePath)
        // .DATA is deprecated in API 29
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }
}


fun takePhoto(context: Context,arSceneView: ArSceneView,onSuccess: () ->Unit) {

    if (!hasPermission(context)) return

    val filename = generateFileName();

    val bitmap = Bitmap.createBitmap(
        arSceneView.getWidth(), arSceneView.getHeight(),
        Bitmap.Config.ARGB_8888
    );

    val handlerThread = HandlerThread("PixelCopier");
    handlerThread.start();
    PixelCopy.request(arSceneView, bitmap, {
        if (it == PixelCopy.SUCCESS) {

            saveImage(bitmap, context, filename);

            val photoFile = File(getDirectory(), filename);
            (context as AppCompatActivity)
                .runOnUiThread({  onSuccess() })

            openPhoto(context, photoFile)
        } else {
            val toast = Toast.makeText(
                context,
                "faild to save the image 😕 " + it, Toast.LENGTH_LONG
            );
            toast.show();
        }
        handlerThread.quitSafely();
    }, Handler(handlerThread.getLooper()));
}



fun getDirectory(): String {
    return Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES
    ).toString() + separator + folderName
}

fun generateFileName(): String {
    return System.currentTimeMillis().toString() + ".png"
}


private fun contentValues(): ContentValues {
    val values = ContentValues()
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
    values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
    if (android.os.Build.VERSION.SDK_INT >= 29) {
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
    }
    return values
}

private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
    if (outputStream != null) {
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

