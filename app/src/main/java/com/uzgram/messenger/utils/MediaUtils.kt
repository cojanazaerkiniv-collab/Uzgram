package com.uzgram.messenger.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.*
import java.util.UUID

object MediaUtils {

    /**
     * Compress a bitmap to JPEG with the given quality (0-100).
     * Returns a ByteArray suitable for upload.
     */
    fun compressBitmap(bitmap: Bitmap, quality: Int = 80): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }

    /**
     * Decode a Uri to a downscaled Bitmap.
     * [maxDimension] limits width and height to avoid OOM.
     */
    fun decodeSampledBitmap(context: Context, uri: Uri, maxDimension: Int = 1280): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val opts = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                    BitmapFactory.decodeStream(stream, null, this)
                }

                opts.inSampleSize = calculateInSampleSize(opts.outWidth, opts.outHeight, maxDimension)
                opts.inJustDecodeBounds = false

                context.contentResolver.openInputStream(uri)?.use { s2 ->
                    BitmapFactory.decodeStream(s2, null, opts)
                }
            }
        } catch (e: Exception) { null }
    }

    private fun calculateInSampleSize(width: Int, height: Int, max: Int): Int {
        var inSampleSize = 1
        if (height > max || width > max) {
            val halfHeight = height / 2
            val halfWidth  = width  / 2
            while (halfHeight / inSampleSize >= max && halfWidth / inSampleSize >= max) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Copy a Uri content to a temp file and return its path.
     * Useful before uploading media files.
     */
    fun uriToTempFile(context: Context, uri: Uri, extension: String = "jpg"): File? {
        return try {
            val tempFile = File(context.cacheDir, "upload_${UUID.randomUUID()}.$extension")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) { null }
    }

    /**
     * Get MIME type from Uri.
     */
    fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }

    /**
     * Get file name from Uri.
     */
    fun getFileName(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            }
        } catch (e: Exception) { null }
    }

    /**
     * Get file size in bytes from Uri.
     */
    fun getFileSize(context: Context, uri: Uri): Long? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                cursor.moveToFirst()
                cursor.getLong(sizeIndex)
            }
        } catch (e: Exception) { null }
    }

    /**
     * Format a duration in milliseconds to MM:SS.
     */
    fun formatDuration(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val secs    = seconds % 60
        return "%d:%02d".format(minutes, secs)
    }
}
