package com.ebc.crud_notes.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun saveImageToInternalStorage(context: Context, uri: Uri) : String? {
    var path: String? = null

    try {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = "note_${System.currentTimeMillis()}.jpg"

        val file = File(context.filesDir, fileName)

        inputStream.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        path = file.absolutePath

    } catch (e: Exception) {
        e.printStackTrace()
    }

    return path
}