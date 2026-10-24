package com.file.material.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.file.material.data.model.FileItem
import com.file.material.ui.theme.IconContainerShapeExpressive
import com.file.material.utils.ThumbnailLoader

private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "heic")
private val THUMBNAIL_EXTENSIONS = setOf(
    "mp3", "flac", "m4a", "ogg", "opus", "wav", "aac",
    "pdf",
    "apk", "xapk",
    "mp4", "mkv", "webm", "avi", "mov", "3gp"
)

@Composable
fun FileThumbnail(
    item: FileItem,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    iconSize: Dp = 24.dp,
    fallbackSize: Dp? = null
) {
    val context = LocalContext.current
    val (icon, tint, bgColor) = getFileVisuals(item)

    val fallbackModifier = if (fallbackSize != null) {
        Modifier.size(fallbackSize).clip(IconContainerShapeExpressive).background(bgColor)
    } else {
        modifier.background(bgColor)
    }

    if (item.isDirectory) {
        Box(
            modifier = fallbackModifier,
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(iconSize)
            )
        }
        return
    }

    val ext = item.extension.lowercase()

    if (ext in IMAGE_EXTENSIONS) {
        var isImageError by remember(item.path) { mutableStateOf(false) }

        if (isImageError) {
            Box(
                modifier = fallbackModifier,
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(iconSize)
                )
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(item.file)
                    .crossfade(true)
                    .build(),
                contentDescription = item.name,
                contentScale = contentScale,
                onError = { isImageError = true },
                modifier = modifier
            )
        }
    } else if (ext in THUMBNAIL_EXTENSIONS) {
        var bitmap by remember(item.path, item.lastModified) {
            mutableStateOf(ThumbnailLoader.getFromCache(item.file))
        }

        LaunchedEffect(item.path, item.lastModified) {
            if (bitmap == null) {
                bitmap = ThumbnailLoader.loadThumbnail(context, item.file, ext)
            }
        }

        val currentBitmap = bitmap
        if (currentBitmap != null) {
            Image(
                bitmap = currentBitmap.asImageBitmap(),
                contentDescription = item.name,
                contentScale = contentScale,
                modifier = modifier
            )
        } else {
            Box(
                modifier = fallbackModifier,
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    } else {
        Box(
            modifier = fallbackModifier,
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
