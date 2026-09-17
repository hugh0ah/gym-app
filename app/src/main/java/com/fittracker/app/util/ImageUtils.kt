package com.fittracker.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object ImageUtils {

    suspend fun uriToBase64(
        context: Context,
        uri: Uri,
        maxDimension: Int = 1280
    ): String? = withContext(Dispatchers.IO) {
        try {
            // Intentar persistir permiso si el URI lo permite (ej: PhotoPicker o Storage Access Framework)
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Throwable) {
                // Si el ContentProvider no soporta persistencia directa, se ignora
            }

            val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        val width = info.size.width
                        val height = info.size.height
                        if (width > maxDimension || height > maxDimension) {
                            val ratio = width.toFloat() / height.toFloat()
                            val targetW = if (ratio > 1) maxDimension else (maxDimension * ratio).toInt()
                            val targetH = if (ratio > 1) (maxDimension / ratio).toInt() else maxDimension
                            decoder.setTargetSize(targetW.coerceAtLeast(1), targetH.coerceAtLeast(1))
                        }
                    }
                } catch (_: Throwable) {
                    decodeWithBitmapFactory(context, uri, maxDimension)
                }
            } else {
                decodeWithBitmapFactory(context, uri, maxDimension)
            }

            if (bitmap == null) return@withContext null

            val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val targetW = if (ratio > 1) maxDimension else (maxDimension * ratio).toInt()
                val targetH = if (ratio > 1) (maxDimension / ratio).toInt() else maxDimension
                Bitmap.createScaledBitmap(bitmap, targetW.coerceAtLeast(1), targetH.coerceAtLeast(1), true)
            } else {
                bitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    private fun decodeWithBitmapFactory(context: Context, uri: Uri, maxDimension: Int): Bitmap? {
        return try {
            val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, boundsOptions)

            var sampleSize = 1
            while (boundsOptions.outWidth / sampleSize > maxDimension || boundsOptions.outHeight / sampleSize > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOptions)
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }
}
