package com.file.material.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.file.material.data.model.FileItem
import com.file.material.ui.theme.CategoryApksColor
import com.file.material.ui.theme.CategoryArchivesColor
import com.file.material.ui.theme.CategoryAudioColor
import com.file.material.ui.theme.CategoryDocsColor
import com.file.material.ui.theme.CategoryImagesColor
import com.file.material.ui.theme.CategoryVideosColor
import com.file.material.ui.theme.IconContainerShapeExpressive
import com.file.material.utils.FileUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    item: FileItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, tint, bgColor) = getFileVisuals(item)

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isSelected) {
                    Modifier.liquidGlass(
                        shape = RoundedCornerShape(16.dp),
                        tintColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        elevation = 3.dp,
                        isDark = isDark
                    )
                } else {
                    Modifier
                }
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            FileThumbnail(
                item = item,
                modifier = Modifier
                    .size(46.dp)
                    .clip(IconContainerShapeExpressive),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                iconSize = 24.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (item.isDirectory) {
                        "${item.childCount} items · ${FileUtils.formatDate(item.lastModified)}"
                    } else {
                        "${FileUtils.formatSize(item.size)} · ${FileUtils.formatDate(item.lastModified)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!isSelectionMode) {
                IconButton(onClick = onMoreClick) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Actions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun getFileVisuals(item: FileItem): Triple<ImageVector, Color, Color> {
    return if (item.isDirectory) {
        Triple(
            Icons.Rounded.Folder,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        )
    } else {
        when (item.extension) {
            "jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "svg" -> Triple(
                Icons.Rounded.Image,
                CategoryImagesColor,
                CategoryImagesColor.copy(alpha = 0.16f)
            )
            "mp4", "mkv", "webm", "avi", "mov", "3gp" -> Triple(
                Icons.Rounded.Movie,
                CategoryVideosColor,
                CategoryVideosColor.copy(alpha = 0.16f)
            )
            "mp3", "wav", "m4a", "flac", "aac", "ogg", "opus" -> Triple(
                Icons.Rounded.Audiotrack,
                CategoryAudioColor,
                CategoryAudioColor.copy(alpha = 0.16f)
            )
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt" -> Triple(
                Icons.Rounded.Description,
                CategoryDocsColor,
                CategoryDocsColor.copy(alpha = 0.16f)
            )
            "apk", "xapk" -> Triple(
                Icons.Rounded.Android,
                CategoryApksColor,
                CategoryApksColor.copy(alpha = 0.16f)
            )
            "zip", "rar", "7z", "tar", "gz" -> Triple(
                Icons.Rounded.FolderZip,
                CategoryArchivesColor,
                CategoryArchivesColor.copy(alpha = 0.16f)
            )
            else -> Triple(
                Icons.AutoMirrored.Rounded.InsertDriveFile,
                MaterialTheme.colorScheme.onSurfaceVariant,
                MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }
    }
}
