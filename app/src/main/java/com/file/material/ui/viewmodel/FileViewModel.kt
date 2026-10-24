package com.file.material.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.file.material.data.model.FileCategory
import com.file.material.data.model.FileItem
import com.file.material.data.model.SortBy
import com.file.material.data.model.SortOrder
import com.file.material.data.model.StorageInfo
import com.file.material.data.model.ViewMode
import com.file.material.data.repository.FileManagerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class FileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FileManagerRepository(application)
    private val rootDir = repository.rootStorageDirectory

    private val _currentDirectory = MutableStateFlow(rootDir)
    val currentDirectory: StateFlow<File> = _currentDirectory.asStateFlow()

    private val _rawFileList = MutableStateFlow<List<FileItem>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortBy = MutableStateFlow(SortBy.NAME)
    val sortBy: StateFlow<SortBy> = _sortBy.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.ASCENDING)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.LIST)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _showHidden = MutableStateFlow(false)
    val showHidden: StateFlow<Boolean> = _showHidden.asStateFlow()

    private val _dynamicColor = MutableStateFlow(true)
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    private val _darkTheme = MutableStateFlow<Boolean?>(null)
    val darkTheme: StateFlow<Boolean?> = _darkTheme.asStateFlow()

    private val _amoledDark = MutableStateFlow(false)
    val amoledDark: StateFlow<Boolean> = _amoledDark.asStateFlow()

    private val _compactView = MutableStateFlow(false)
    val compactView: StateFlow<Boolean> = _compactView.asStateFlow()

    private val _showFileExtensions = MutableStateFlow(true)
    val showFileExtensions: StateFlow<Boolean> = _showFileExtensions.asStateFlow()

    private val _confirmDelete = MutableStateFlow(true)
    val confirmDelete: StateFlow<Boolean> = _confirmDelete.asStateFlow()

    private val _showFullPaths = MutableStateFlow(false)
    val showFullPaths: StateFlow<Boolean> = _showFullPaths.asStateFlow()

    private val _selectedFiles = MutableStateFlow<Set<FileItem>>(emptySet())
    val selectedFiles: StateFlow<Set<FileItem>> = _selectedFiles.asStateFlow()

    private val _storageInfo = MutableStateFlow(StorageInfo())
    val storageInfo: StateFlow<StorageInfo> = _storageInfo.asStateFlow()

    private val _recentFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val recentFiles: StateFlow<List<FileItem>> = _recentFiles.asStateFlow()

    private val _categoryFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val categoryFiles: StateFlow<List<FileItem>> = _categoryFiles.asStateFlow()

    private val _selectedCategory = MutableStateFlow<FileCategory?>(null)
    val selectedCategory: StateFlow<FileCategory?> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Filtered and searched file list
    val fileList: StateFlow<List<FileItem>> = combine(_rawFileList, _searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val breadcrumbs: StateFlow<List<File>> = combine(_currentDirectory) { dirs ->
        val dir = dirs.first()
        val crumbs = mutableListOf<File>()
        var current: File? = dir
        while (current != null) {
            crumbs.add(0, current)
            if (current.absolutePath == rootDir.absolutePath) {
                break
            }
            current = current.parentFile
        }
        crumbs
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(rootDir))

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _storageInfo.value = repository.getStorageInfo()
            _rawFileList.value = repository.listFiles(
                directory = _currentDirectory.value,
                showHidden = _showHidden.value,
                sortBy = _sortBy.value,
                sortOrder = _sortOrder.value
            )
            _recentFiles.value = repository.getRecentFiles()
            _isLoading.value = false
        }
    }

    fun navigateTo(directory: File) {
        if (directory.exists() && directory.isDirectory) {
            _currentDirectory.value = directory
            _searchQuery.value = ""
            clearSelection()
            refresh()
        }
    }

    fun navigateUp(): Boolean {
        val parent = _currentDirectory.value.parentFile
        if (parent != null && _currentDirectory.value.absolutePath != rootDir.absolutePath) {
            navigateTo(parent)
            return true
        }
        return false
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortBy(sort: SortBy) {
        if (_sortBy.value == sort) {
            _sortOrder.value = if (_sortOrder.value == SortOrder.ASCENDING) SortOrder.DESCENDING else SortOrder.ASCENDING
        } else {
            _sortBy.value = sort
            _sortOrder.value = SortOrder.ASCENDING
        }
        refresh()
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun setShowHidden(show: Boolean) {
        _showHidden.value = show
        refresh()
    }

    fun setDynamicColor(enabled: Boolean) {
        _dynamicColor.value = enabled
    }

    fun setDarkTheme(dark: Boolean?) {
        _darkTheme.value = dark
    }

    fun toggleFileSelection(item: FileItem) {
        val current = _selectedFiles.value.toMutableSet()
        if (current.contains(item)) {
            current.remove(item)
        } else {
            current.add(item)
        }
        _selectedFiles.value = current
    }

    fun selectAll() {
        _selectedFiles.value = fileList.value.toSet()
    }

    fun clearSelection() {
        _selectedFiles.value = emptySet()
    }

    fun loadCategory(category: FileCategory) {
        _selectedCategory.value = category
        viewModelScope.launch {
            _isLoading.value = true
            _categoryFiles.value = repository.getFilesForCategory(category)
            _isLoading.value = false
        }
    }

    fun createFolder(name: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.createFolder(_currentDirectory.value, name)
            if (success) refresh()
            onResult(success)
        }
    }

    fun renameFile(file: File, newName: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.rename(file, newName)
            if (success) refresh()
            onResult(success)
        }
    }

    fun deleteFile(file: File, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.delete(file)
            if (success) refresh()
            onResult(success)
        }
    }

    fun deleteSelected(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val list = _selectedFiles.value.map { it.file }
            val success = repository.deleteMultiple(list)
            clearSelection()
            if (success) refresh()
            onResult(success)
        }
    }

    fun setAmoledDark(enabled: Boolean) {
        _amoledDark.value = enabled
    }

    fun setCompactView(enabled: Boolean) {
        _compactView.value = enabled
    }

    fun setShowFileExtensions(enabled: Boolean) {
        _showFileExtensions.value = enabled
    }

    fun setConfirmDelete(enabled: Boolean) {
        _confirmDelete.value = enabled
    }

    fun setShowFullPaths(enabled: Boolean) {
        _showFullPaths.value = enabled
    }

    fun cleanTemporaryFiles(onResult: (Long, Int) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val (bytes, count) = repository.cleanTemporaryFiles()
            _storageInfo.value = repository.getStorageInfo()
            _isLoading.value = false
            onResult(bytes, count)
        }
    }

    fun resetSettingsToDefault() {
        _dynamicColor.value = true
        _darkTheme.value = null
        _amoledDark.value = false
        _showHidden.value = false
        _compactView.value = false
        _showFileExtensions.value = true
        _confirmDelete.value = true
        _showFullPaths.value = false
        _sortBy.value = SortBy.NAME
        _sortOrder.value = SortOrder.ASCENDING
        _viewMode.value = ViewMode.LIST
        refresh()
    }
}

