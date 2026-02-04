package com.ichaiwizm.metaraybanassistant.data.source

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

/**
 * Helper pour installer un APK
 */
object ApkInstaller {
    /**
     * Lance l'installation d'un APK
     * @param context Le context Android
     * @param apkFile Le fichier APK à installer
     */
    fun installApk(context: Context, apkFile: File) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION

            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Android 7.0+ : Utiliser FileProvider
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
            } else {
                Uri.fromFile(apkFile)
            }

            setDataAndType(uri, "application/vnd.android.package-archive")
        }

        context.startActivity(intent)
    }
}
