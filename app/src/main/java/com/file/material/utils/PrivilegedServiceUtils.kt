package com.file.material.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import java.io.File

data class PrivilegedAppInfo(
    val name: String,
    val packageName: String,
    val isInstalled: Boolean,
    val downloadUrl: String,
    val description: String
)

object PrivilegedServiceUtils {

    private val KNOWN_PRIVILEGED_APPS = listOf(
        PrivilegedAppInfo(
            name = "Shizuku",
            packageName = "moe.shizuku.privileged.api",
            isInstalled = false,
            downloadUrl = "https://shizuku.rikka.app",
            description = "Standard system service binder for elevated permissions."
        ),
        PrivilegedAppInfo(
            name = "Stellar",
            packageName = "roro.stellar.manager",
            isInstalled = false,
            downloadUrl = "https://github.com/roro2239/Stellar",
            description = "Modern Material You Shizuku fork with extended capabilities."
        ),
        PrivilegedAppInfo(
            name = "Dhizuku",
            packageName = "com.rosan.dhizuku",
            isInstalled = false,
            downloadUrl = "https://github.com/iamr0s/Dhizuku",
            description = "Device Owner privileged API provider."
        ),
        PrivilegedAppInfo(
            name = "Shizuku+",
            packageName = "moe.shizuku.privileged.api.plus",
            isInstalled = false,
            downloadUrl = "https://github.com/RikkaApps/Shizuku",
            description = "Enhanced Shizuku variant with additional root hooks."
        )
    )

    fun getPrivilegedApps(context: Context): List<PrivilegedAppInfo> {
        val pm = context.packageManager
        return KNOWN_PRIVILEGED_APPS.map { app ->
            val installed = try {
                pm.getPackageInfo(app.packageName, 0) != null
            } catch (e: Exception) {
                // Also check alternative known package names for Stellar
                if (app.name == "Stellar") {
                    listOf("roro.stellar.manager", "com.stellar.app", "io.github.a13e300.stellar", "com.stellar.shizuku").any { pkg ->
                        try {
                            pm.getPackageInfo(pkg, 0) != null
                        } catch (_: Exception) {
                            false
                        }
                    }
                } else {
                    false
                }
            }
            app.copy(isInstalled = installed)
        }
    }

    fun isRootAvailable(): Boolean {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/vendor/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su"
        )
        return try {
            paths.any { File(it).exists() }
        } catch (_: Exception) {
            false
        }
    }

    fun launchApp(context: Context, packageName: String): Boolean {
        return try {
            var intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent == null && (packageName == "roro.stellar.manager" || packageName.contains("stellar"))) {
                for (altPkg in listOf("roro.stellar.manager", "com.stellar.app", "io.github.a13e300.stellar", "com.stellar.shizuku")) {
                    intent = context.packageManager.getLaunchIntentForPackage(altPkg)
                    if (intent != null) break
                }
            }
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                Toast.makeText(context, "Could not open app", Toast.LENGTH_SHORT).show()
                false
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error launching app: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
        }
    }
}
