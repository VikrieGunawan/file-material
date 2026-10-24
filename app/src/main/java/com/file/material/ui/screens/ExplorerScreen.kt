package com.file.material.ui.screens

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.file.material.R
import com.file.material.data.model.FileItem
import com.file.material.data.model.SortBy
import com.file.material.data.model.ViewMode
import com.file.material.ui.components.BreadcrumbBar
import com.file.material.ui.components.FileGridItem
import com.file.material.ui.components.FileListItem
import com.file.material.ui.theme.ButtonShapeExpressive
import com.file.material.ui.viewmodel.FileViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerScreen(
    viewModel: FileViewModel,
    onNavigateBack: () -> Unit,
    onFileClick: (FileItem) -> Unit,
    onFileMoreClick: (FileItem) -> Unit,
    onCreateFolderClick: () -> Unit,
    onDeleteSelectedClick: () -> Unit,
    onShareSelectedClick: (List<FileItem>) -> Unit,
    onRestrictedFolder: (File) -> Unit
) {
    val currentDirectory by viewModel.currentDirectory.collectAsState()
    val breadcrumbs by viewModel.breadcrumbs.collectAsState()
    val fileList by viewModel.fileList.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var isSearchActive by remember { mutableStateOf(false) }
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    val isSelectionMode = selectedFiles.isNotEmpty()

    val isCurrentRestricted = remember(currentDirectory) {
        val path = currentDirectory.absolutePath
        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) &&
                (path.endsWith("/Android/data") || path.endsWith("/Android/obb") ||
                 path.contains("/Android/data/") || path.contains("/Android/obb/"))
    }

    BackHandler {
        if (isSelectionMode) {
            viewModel.clearSelection()
        } else if (isSearchActive) {
            isSearchActive = false
            viewModel.setSearchQuery("")
        } else {
            val handled = viewModel.navigateUp()
            if (!handled) {
                onNavigateBack()
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = {
                        Text(
                            text = "${selectedFiles.size} selected",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.selectAll() }) {
                            Icon(Icons.Rounded.SelectAll, contentDescription = "Select all")
                        }
                        IconButton(onClick = {
                            val items = selectedFiles.filter { !it.isDirectory }
                            if (items.isNotEmpty()) onShareSelectedClick(items)
                        }) {
                            Icon(Icons.Rounded.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = onDeleteSelectedClick) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            } else if (isSearchActive) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            isSearchActive = false
                            viewModel.setSearchQuery("")
                        }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text(stringResource(R.string.search_files_hint)) },
                            singleLine = true,
                            shape = ButtonShapeExpressive,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear")
                            }
                        }
                    }
                }
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = currentDirectory.name.ifBlank { "Files" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            val handled = viewModel.navigateUp()
                            if (!handled) onNavigateBack()
                        }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Rounded.Search, contentDescription = "Search")
                        }

                        // ViewMode Toggle (List / Grid)
                        IconButton(onClick = {
                            viewModel.setViewMode(
                                if (viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
                            )
                        }) {
                            Icon(
                                imageVector = if (viewMode == ViewMode.LIST) Icons.Rounded.GridView else Icons.Rounded.ViewList,
                                contentDescription = "Toggle view"
                            )
                        }

                        // Sort Menu
                        Box {
                            IconButton(onClick = { isSortMenuExpanded = true }) {
                                Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = "Sort")
                            }
                            DropdownMenu(
                                expanded = isSortMenuExpanded,
                                onDismissRequest = { isSortMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Name ${if (sortBy == SortBy.NAME) "✓" else ""}") },
                                    onClick = {
                                        viewModel.setSortBy(SortBy.NAME)
                                        isSortMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Date Modified ${if (sortBy == SortBy.DATE) "✓" else ""}") },
                                    onClick = {
                                        viewModel.setSortBy(SortBy.DATE)
                                        isSortMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Size ${if (sortBy == SortBy.SIZE) "✓" else ""}") },
                                    onClick = {
                                        viewModel.setSortBy(SortBy.SIZE)
                                        isSortMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Type ${if (sortBy == SortBy.TYPE) "✓" else ""}") },
                                    onClick = {
                                        viewModel.setSortBy(SortBy.TYPE)
                                        isSortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = onCreateFolderClick,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Rounded.CreateNewFolder, contentDescription = "New Folder")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!isSearchActive) {
                BreadcrumbBar(
                    breadcrumbs = breadcrumbs,
                    currentDirectory = currentDirectory,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }

            // Restricted folder banner
            if (isCurrentRestricted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onRestrictedFolder(currentDirectory) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Restricted Android Folder",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Android blocks direct access to this folder. Full access requires Shizuku. Tap to learn more.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (fileList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSearchActive) stringResource(R.string.empty_search)
                        else if (isCurrentRestricted) "Access restricted by Android OS.\nPair with Shizuku for full access."
                        else stringResource(R.string.empty_directory),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                if (viewMode == ViewMode.LIST) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        items(fileList, key = { it.path }) { item ->
                            val isSelected = selectedFiles.contains(item)
                            FileListItem(
                                item = item,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onClick = {
                                    if (isSelectionMode) {
                                        viewModel.toggleFileSelection(item)
                                    } else if (item.isDirectory) {
                                        val path = item.file.absolutePath
                                        val isRestricted = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) &&
                                                (path.endsWith("/Android/data") || path.endsWith("/Android/obb") ||
                                                 (item.name.equals("data", ignoreCase = true) && item.file.parentFile?.name.equals("Android", ignoreCase = true)) ||
                                                 (item.name.equals("obb", ignoreCase = true) && item.file.parentFile?.name.equals("Android", ignoreCase = true)))

                                        if (isRestricted && (item.file.listFiles() == null || item.file.listFiles()?.isEmpty() == true)) {
                                            onRestrictedFolder(item.file)
                                        } else {
                                            viewModel.navigateTo(item.file)
                                        }
                                    } else {
                                        onFileClick(item)
                                    }
                                },
                                onLongClick = {
                                    viewModel.toggleFileSelection(item)
                                },
                                onMoreClick = {
                                    onFileMoreClick(item)
                                }
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(fileList, key = { it.path }) { item ->
                            val isSelected = selectedFiles.contains(item)
                            FileGridItem(
                                item = item,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onClick = {
                                    if (isSelectionMode) {
                                        viewModel.toggleFileSelection(item)
                                    } else if (item.isDirectory) {
                                        val path = item.file.absolutePath
                                        val isRestricted = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) &&
                                                (path.endsWith("/Android/data") || path.endsWith("/Android/obb") ||
                                                 (item.name.equals("data", ignoreCase = true) && item.file.parentFile?.name.equals("Android", ignoreCase = true)) ||
                                                 (item.name.equals("obb", ignoreCase = true) && item.file.parentFile?.name.equals("Android", ignoreCase = true)))

                                        if (isRestricted && (item.file.listFiles() == null || item.file.listFiles()?.isEmpty() == true)) {
                                            onRestrictedFolder(item.file)
                                        } else {
                                            viewModel.navigateTo(item.file)
                                        }
                                    } else {
                                        onFileClick(item)
                                    }
                                },
                                onLongClick = {
                                    viewModel.toggleFileSelection(item)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
