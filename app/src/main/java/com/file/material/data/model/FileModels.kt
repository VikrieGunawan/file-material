package com.file.material.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.file.material.ui.theme.CategoryApksColor
import com.file.material.ui.theme.CategoryArchivesColor
import com.file.material.ui.theme.CategoryAudioColor
import com.file.material.ui.theme.CategoryDocsColor
import com.file.material.ui.theme.CategoryDownloadsColor
import com.file.material.ui.theme.CategoryImagesColor
import com.file.material.ui.theme.CategoryVideosColor
import java.io.File

data class FileItem(
    val file: File,
    val name: String = file.name,
    val path: String = file.absolutePath,
    val isDirectory: Boolean = file.isDirectory,
    val size: Long = if (file.isDirectory) 0L else file.length(),
    val lastModified: Long = file.lastModified(),
    val extension: String = file.extension.lowercase(),
    val mimeType: String = "",
    val isHidden: Boolean = file.isHidden || file.name.startsWith("."),
    val childCount: Int = if (file.isDirectory) (file.list()?.size ?: 0) else 0
)

data class StorageInfo(
    val totalBytes: Long = 0L,
    val freeBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val usedPercentage: Float = 0f,
    val totalFormatted: String = "0 GB",
    val freeFormatted: String = "0 GB",
    val usedFormatted: String = "0 GB"
)

enum class FileCategory(
    val title: String,
    val icon: ImageVector,
    val accentColor: Color,
    val extensions: Set<String>
) {
    IMAGES(
        title = "Images",
        icon = Icons.Rounded.Image,
        accentColor = CategoryImagesColor,
        extensions = setOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "svg")
    ),
    VIDEOS(
        title = "Videos",
        icon = Icons.Rounded.Movie,
        accentColor = CategoryVideosColor,
        extensions = setOf("mp4", "mkv", "webm", "avi", "3gp", "mov", "flv")
    ),
    AUDIO(
        title = "Audio",
        icon = Icons.Rounded.Audiotrack,
        accentColor = CategoryAudioColor,
        extensions = setOf("mp3", "wav", "m4a", "flac", "aac", "ogg", "opus")
    ),
    DOCUMENTS(
        title = "Documents",
        icon = Icons.Rounded.Description,
        accentColor = CategoryDocsColor,
        extensions = setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "epub")
    ),
    DOWNLOADS(
        title = "Downloads",
        icon = Icons.Rounded.Download,
        accentColor = CategoryDownloadsColor,
        extensions = emptySet() // Special folder matching
    ),
    APKS(
        title = "APKs",
        icon = Icons.Rounded.Android,
        accentColor = CategoryApksColor,
        extensions = setOf("apk", "xapk", "apks")
    ),
    ARCHIVES(
        title = "Archives",
        icon = Icons.Rounded.FolderZip,
        accentColor = CategoryArchivesColor,
        extensions = setOf("zip", "rar", "7z", "tar", "gz", "bz2")
    )
}

enum class SortBy {
    NAME,
    DATE,
    SIZE,
    TYPE
}

enum class SortOrder {
    ASCENDING,
    DESCENDING
}

enum class ViewMode {
    LIST,
    GRID
}
