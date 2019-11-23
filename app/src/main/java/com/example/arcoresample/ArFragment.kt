package com.example.arcoresample

import android.Manifest
import com.google.ar.sceneform.ux.ArFragment


/**
 *Created by Ayat Khriasat on 22,November,2019 at 19:57
 *Email: ayatzkhraisat@gmail.com
 *Project: ARCoreSample
 **/
class ArFragment : ArFragment() {

    override fun getAdditionalPermissions(): Array<String?>? {
        val additionalPermissions = super.getAdditionalPermissions()
        val permissionLength =
            additionalPermissions?.size ?: 0
        val permissions =
            arrayOfNulls<String>(permissionLength + 1)
        permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (permissionLength > 0) {
            System.arraycopy(
                additionalPermissions!!,
                0,
                permissions,
                1,
                additionalPermissions.size
            )
        }
        return permissions
    }


}