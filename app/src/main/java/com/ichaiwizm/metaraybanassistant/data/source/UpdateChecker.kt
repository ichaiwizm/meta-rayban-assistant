package com.ichaiwizm.metaraybanassistant.data.source

import com.google.gson.Gson
import com.ichaiwizm.metaraybanassistant.data.model.AppVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Service pour vérifier les mises à jour de l'app
 */
class UpdateChecker(
    private val updateJsonUrl: String = "https://raw.githubusercontent.com/ichaiwizm/meta-rayban-assistant/master/version.json"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    /**
     * Vérifie s'il y a une nouvelle version disponible
     * @param currentVersionCode Le versionCode actuel de l'app
     * @return AppVersion si une mise à jour est disponible, null sinon
     */
    suspend fun checkForUpdate(currentVersionCode: Int): Result<AppVersion?> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(updateJsonUrl)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Erreur HTTP: ${response.code}")
                )
            }

            val jsonString = response.body?.string()
                ?: return@withContext Result.failure(Exception("Réponse vide"))

            val latestVersion = gson.fromJson(jsonString, AppVersion::class.java)

            // Vérifier si une mise à jour est disponible
            if (latestVersion.versionCode > currentVersionCode) {
                Result.success(latestVersion)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Télécharge l'APK depuis l'URL fournie
     * @param url L'URL de l'APK
     * @param onProgress Callback pour suivre la progression (0-100)
     * @return Le chemin du fichier téléchargé ou une erreur
     */
    suspend fun downloadApk(
        url: String,
        outputFile: java.io.File,
        onProgress: (Int) -> Unit = {}
    ): Result<java.io.File> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Erreur de téléchargement: ${response.code}")
                )
            }

            val contentLength = response.body?.contentLength() ?: -1
            val inputStream = response.body?.byteStream()
                ?: return@withContext Result.failure(Exception("Pas de données à télécharger"))

            outputFile.outputStream().use { output ->
                val buffer = ByteArray(8192)
                var totalBytesRead = 0L
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    if (contentLength > 0) {
                        val progress = (totalBytesRead * 100 / contentLength).toInt()
                        onProgress(progress)
                    }
                }
            }

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
