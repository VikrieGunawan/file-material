package com.file.material.data.model

import android.graphics.drawable.Drawable

data class AppLauncherInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable?
)
