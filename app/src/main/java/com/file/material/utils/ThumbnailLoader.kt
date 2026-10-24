package com.file.material.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfRenderer
import android.media.MediaMetadataRetriever
import android.os.ParcelFileDescriptor
import android.util.LruCache
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ThumbnailLoader {

    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = (maxMemory / 8).coerceAtLeast(1024 * 16) // at least 16MB
    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    private fun getCacheKey(file: File): String {
        return "${file.absolutePath}:${file.lastModified()}"
    }

    fun getFromCache(file: File): Bitmap? {
        return memoryCache.get(getCacheKey(file))
    }

    suspend fun loadThumbnail(context: Context, file: File, extension: String): Bitmap? = withContext(Dispatchers.IO) {
        val key = getCacheKey(file)
        memoryCache.get(key)?.let { return@withContext it }

        if (!file.exists() || !file.canRead() || file.length() == 0L) return@withContext null

        val bitmap = try {
            when (extension.lowercase()) {
                "mp3", "flac", "m4a", "ogg", "opus", "wav", "aac" -> loadAudioCover(file)
                "pdf" -> loadPdfPage(file)
                "apk", "xapk" -> loadApkIcon(context, file)
                "mp4", "mkv", "webm", "avi", "mov", "3gp" -> loadVideoFrame(file)
                else -> null
            }
        } catch (_: Exception) {
            null
        }

        if (bitmap != null) {
            memoryCache.put(key, bitmap)
        }
        bitmap
    }

    private fun loadAudioCover(file: File): Bitmap? {
        val mmr = MediaMetadataRetriever()
        return try {
            mmr.setDataSource(file.absolutePath)
            val art = mmr.embeddedPicture ?: return null
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(art, 0, art.size, options)
            options.inSampleSize = calculateInSampleSize(options, 400, 400)
            options.inJustDecodeBounds = false
            BitmapFactory.decodeByteArray(art, 0, art.size, options)
        } catch (_: Exception) {
            null
        } finally {
            try { mmr.release() } catch (_: Exception) {}
        }
    }

    private fun loadPdfPage(file: File): Bitmap? {
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        var page: PdfRenderer.Page? = null
        return try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            if (renderer.pageCount <= 0) return null
            page = renderer.openPage(0)

            val targetWidth = 400
            val targetHeight = ((targetWidth.toFloat() / page.width) * page.height).toInt().coerceAtLeast(100)
            val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap
        } catch (_: Exception) {
            null
        } finally {
            try { page?.close() } catch (_: Exception) {}
            try { renderer?.close() } catch (_: Exception) {}
            try { pfd?.close() } catch (_: Exception) {}
        }
    }

    private fun loadApkIcon(context: Context, file: File): Bitmap? {
        return try {
            val pm = context.packageManager
            val pi = pm.getPackageArchiveInfo(file.absolutePath, 0) ?: return null
            val appInfo = pi.applicationInfo ?: return null
            appInfo.sourceDir = file.absolutePath
            appInfo.publicSourceDir = file.absolutePath
            val iconDrawable: Drawable = appInfo.loadIcon(pm) ?: return null
            if (iconDrawable is BitmapDrawable && iconDrawable.bitmap != null) {
                iconDrawable.bitmap
            } else {
                iconDrawable.toBitmap(
                    width = iconDrawable.intrinsicWidth.coerceAtLeast(96),
                    height = iconDrawable.intrinsicHeight.coerceAtLeast(96),
                    config = Bitmap.Config.ARGB_8888
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun loadVideoFrame(file: File): Bitmap? {
        val mmr = MediaMetadataRetriever()
        return try {
            mmr.setDataSource(file.absolutePath)
            mmr.getFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC) ?: mmr.frameAtTime
        } catch (_: Exception) {
            null
        } finally {
            try { mmr.release() } catch (_: Exception) {}
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
