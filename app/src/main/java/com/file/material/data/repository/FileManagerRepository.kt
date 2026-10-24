package com.file.material.data.repository

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.file.material.data.model.FileCategory
import com.file.material.data.model.FileItem
import com.file.material.data.model.SortBy
import com.file.material.data.model.SortOrder
import com.file.material.data.model.StorageInfo
import com.file.material.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileManagerRepository(private val context: Context) {

    val rootStorageDirectory: File
        get() = Environment.getExternalStorageDirectory()

    suspend fun getStorageInfo(): StorageInfo = withContext(Dispatchers.IO) {
        try {
            val root = Environment.getExternalStorageDirectory()
            val stat = StatFs(root.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val freeBytes = availableBlocks * blockSize
            val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
            val usedPercentage = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()) else 0f

            StorageInfo(
                totalBytes = totalBytes,
                freeBytes = freeBytes,
                usedBytes = usedBytes,
                usedPercentage = usedPercentage,
                totalFormatted = FileUtils.formatSize(totalBytes),
                freeFormatted = FileUtils.formatSize(freeBytes),
                usedFormatted = FileUtils.formatSize(usedBytes)
            )
        } catch (e: Exception) {
            StorageInfo()
        }
    }

    suspend fun listFiles(
        directory: File,
        showHidden: Boolean = false,
        sortBy: SortBy = SortBy.NAME,
        sortOrder: SortOrder = SortOrder.ASCENDING
    ): List<FileItem> = withContext(Dispatchers.IO) {
        if (!directory.exists() || !directory.isDirectory) {
            return@withContext emptyList()
        }

        val rawFiles = directory.listFiles() ?: return@withContext emptyList()
        val items = rawFiles
            .filter { showHidden || (!it.isHidden && !it.name.startsWith(".")) }
            .map { file ->
                FileItem(
                    file = file,
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = file.isDirectory,
                    size = if (file.isDirectory) 0L else file.length(),
                    lastModified = file.lastModified(),
                    extension = file.extension.lowercase(),
                    mimeType = FileUtils.getMimeType(file),
                    isHidden = file.isHidden || file.name.startsWith("."),
                    childCount = if (file.isDirectory) (file.list()?.size ?: 0) else 0
                )
            }

        // Directories first, then files sorted according to preference
        val sortedList = items.sortedWith { a, b ->
            if (a.isDirectory && !b.isDirectory) {
                -1
            } else if (!a.isDirectory && b.isDirectory) {
                1
            } else {
                val comparison = when (sortBy) {
                    SortBy.NAME -> a.name.compareTo(b.name, ignoreCase = true)
                    SortBy.DATE -> a.lastModified.compareTo(b.lastModified)
                    SortBy.SIZE -> a.size.compareTo(b.size)
                    SortBy.TYPE -> a.extension.compareTo(b.extension, ignoreCase = true)
                }
                if (sortOrder == SortOrder.ASCENDING) comparison else -comparison
            }
        }

        sortedList
    }

    suspend fun getRecentFiles(limit: Int = 20): List<FileItem> = withContext(Dispatchers.IO) {
        val root = Environment.getExternalStorageDirectory()
        val candidateDirs = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            root
        )

        val collected = mutableListOf<FileItem>()
        for (dir in candidateDirs) {
            if (dir != null && dir.exists() && dir.isDirectory) {
                dir.listFiles()?.filter { it.isFile && !it.name.startsWith(".") }?.forEach { f ->
                    collected.add(
                        FileItem(
                            file = f,
                            name = f.name,
                            path = f.absolutePath,
                            isDirectory = false,
                            size = f.length(),
                            lastModified = f.lastModified(),
                            extension = f.extension.lowercase(),
                            mimeType = FileUtils.getMimeType(f)
                        )
                    )
                }
            }
        }

        collected.sortedByDescending { it.lastModified }.take(limit)
    }

    suspend fun getFilesForCategory(category: FileCategory): List<FileItem> = withContext(Dispatchers.IO) {
        val root = Environment.getExternalStorageDirectory()
        val result = mutableListOf<FileItem>()

        if (category == FileCategory.DOWNLOADS) {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadDir != null && downloadDir.exists()) {
                downloadDir.listFiles()?.filter { !it.name.startsWith(".") }?.forEach { f ->
                    result.add(
                        FileItem(
                            file = f,
                            name = f.name,
                            path = f.absolutePath,
                            isDirectory = f.isDirectory,
                            size = if (f.isDirectory) 0L else f.length(),
                            lastModified = f.lastModified(),
                            extension = f.extension.lowercase(),
                            mimeType = FileUtils.getMimeType(f)
                        )
                    )
                }
            }
            return@withContext result.sortedByDescending { it.lastModified }
        }

        // Scan common directories for category matching
        fun scanDir(dir: File, depth: Int = 0) {
            if (depth > 3 || !dir.exists() || !dir.isDirectory || dir.name.startsWith(".")) return
            val files = dir.listFiles() ?: return
            for (f in files) {
                if (f.isDirectory) {
                    if (!f.name.equals("Android", ignoreCase = true)) {
                        scanDir(f, depth + 1)
                    }
                } else if (f.isFile && !f.name.startsWith(".")) {
                    val ext = f.extension.lowercase()
                    if (category.extensions.contains(ext)) {
                        result.add(
                            FileItem(
                                file = f,
                                name = f.name,
                                path = f.absolutePath,
                                isDirectory = false,
                                size = f.length(),
                                lastModified = f.lastModified(),
                                extension = ext,
                                mimeType = FileUtils.getMimeType(f)
                            )
                        )
                    }
                }
            }
        }

        scanDir(root)
        result.sortedByDescending { it.lastModified }
    }

    suspend fun createFolder(parentDir: File, folderName: String): Boolean = withContext(Dispatchers.IO) {
        val newFolder = File(parentDir, folderName.trim())
        if (!newFolder.exists()) {
            newFolder.mkdirs()
        } else false
    }

    suspend fun rename(file: File, newName: String): Boolean = withContext(Dispatchers.IO) {
        val target = File(file.parentFile, newName.trim())
        if (!target.exists()) {
            file.renameTo(target)
        } else false
    }

    suspend fun delete(file: File): Boolean = withContext(Dispatchers.IO) {
        if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }

    suspend fun deleteMultiple(files: List<File>): Boolean = withContext(Dispatchers.IO) {
        var allSuccess = true
        for (f in files) {
            val res = if (f.isDirectory) f.deleteRecursively() else f.delete()
            if (!res) allSuccess = false
        }
        allSuccess
    }
}
