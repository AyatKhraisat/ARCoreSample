package com.example.arcoresample

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import java.io.File

/**
 *Created by Ayat Khriasat on 24,November,2019 at 22:28
 *Email: ayatzkhraisat@gmail.com
 *Project: ARCoreSample
 **/

fun hasWritePermission(context: Context): Boolean {
    return (ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
            == PackageManager.PERMISSION_GRANTED)
}

fun launchPermissionSettings(context: Context) {
    val intent = Intent()
    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    intent.data = Uri.fromParts("package", context.getPackageName(), null)
    context.startActivity(intent)
}

fun hasPermission(context: Context): Boolean {
    if (!hasWritePermission(context)) {
        Toast.makeText(
            context,
            "requires the storage permission",
            Toast.LENGTH_LONG
        )
            .show()
        launchPermissionSettings(context)
        return false
    }
    return true
}


fun openPhoto(context: Context, file: File) {
    val photoURI = FileProvider.getUriForFile(
        context,
        context.getPackageName() + ".provider",
        file
    );
    val intent = Intent(Intent.ACTION_VIEW, photoURI);
    intent.setDataAndType(photoURI, "image/*");
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    context.startActivity(intent);
}
