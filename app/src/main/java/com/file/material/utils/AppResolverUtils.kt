package com.file.material.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.file.material.data.model.AppLauncherInfo
import java.io.File

object AppResolverUtils {

    fun getAppsForFile(context: Context, file: File, mimeTypeOverride: String? = null): List<AppLauncherInfo> {
        val packageManager = context.packageManager
        val uri: Uri = try {
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            return emptyList()
        }

        val mimeType = mimeTypeOverride ?: FileUtils.getMimeType(file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val resolveInfos = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }

        val apps = mutableListOf<AppLauncherInfo>()
        for (info in resolveInfos) {
            val packageName = info.activityInfo.packageName
            // Exclude our own app from opening itself in an infinite loop
            if (packageName == context.packageName) continue

            val label = info.loadLabel(packageManager).toString()
            val icon = info.loadIcon(packageManager)
            val activityName = info.activityInfo.name
            apps.add(
                AppLauncherInfo(
                    label = label,
                    packageName = packageName,
                    activityName = activityName,
                    icon = icon
                )
            )
        }

        return apps.sortedBy { it.label.lowercase() }
    }

    fun launchApp(context: Context, file: File, app: AppLauncherInfo, mimeTypeOverride: String? = null) {
        try {
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val mimeType = mimeTypeOverride ?: FileUtils.getMimeType(file)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                setClassName(app.packageName, app.activityName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not launch ${app.label}: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchSystemChooser(context: Context, file: File, mimeTypeOverride: String? = null) {
        try {
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val mimeType = mimeTypeOverride ?: FileUtils.getMimeType(file)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open with"))
        } catch (e: Exception) {
            Toast.makeText(context, "No app available to open this file", Toast.LENGTH_SHORT).show()
        }
    }

    fun drawableToBitmap(drawable: Drawable?): Bitmap {
        if (drawable == null) {
            return Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
        }
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
