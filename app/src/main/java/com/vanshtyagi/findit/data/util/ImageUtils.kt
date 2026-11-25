package com.vanshtyagi.findit.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

object ImageUtils {

    /**
     * Creates a temporary content URI for CameraX / TakePicture captures.
     */
    fun createTempCameraUri(context: Context): Uri {
        val capturesDir = File(context.cacheDir, "captures").apply { mkdirs() }
        val file = File(capturesDir, "capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Decodes a content URI, corrects EXIF orientation, downscales so max dimension <= 1024px,
     * and writes a compressed JPEG to cache for fast mobile upload.
     */
    fun prepareImageForUpload(context: Context, uri: Uri, maxDim: Int = 1024): File {
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val boundsStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to read selected image.")
        boundsStream.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        }

        val srcWidth = boundsOptions.outWidth
        val srcHeight = boundsOptions.outHeight
        if (srcWidth <= 0 || srcHeight <= 0) {
            throw IllegalArgumentException("Invalid image dimensions.")
        }

        var sampleSize = 1
        while (max(srcWidth / sampleSize, srcHeight / sampleSize) > maxDim * 2) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        var bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: throw IllegalArgumentException("Failed to decode image.")

        // Apply EXIF rotation if present
        val rotationDegrees = context.contentResolver.openInputStream(uri)?.use { stream ->
            val exif = ExifInterface(stream)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f

        if (rotationDegrees != 0f) {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) bitmap.recycle()
            bitmap = rotated
        }

        // Exact downscale to <= maxDim
        val maxSide = max(bitmap.width, bitmap.height)
        if (maxSide > maxDim) {
            val scale = maxDim.toFloat() / maxSide.toFloat()
            val targetW = (bitmap.width * scale).roundToInt().coerceAtLeast(1)
            val targetH = (bitmap.height * scale).roundToInt().coerceAtLeast(1)
            val scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            if (scaled != bitmap) bitmap.recycle()
            bitmap = scaled
        }

        val uploadsDir = File(context.cacheDir, "uploads").apply { mkdirs() }
        val outputFile = File(uploadsDir, "upload_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        bitmap.recycle()
        return outputFile
    }
}
