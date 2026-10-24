package com.file.material

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.file.material.data.model.FileCategory
import com.file.material.data.model.FileItem
import com.file.material.ui.components.CreateFolderDialog
import com.file.material.ui.components.DeleteConfirmDialog
import com.file.material.ui.components.FileActionSheet
import com.file.material.ui.components.FilePropertiesDialog
import com.file.material.ui.components.RenameDialog
import com.file.material.ui.screens.CategoryScreen
import com.file.material.ui.screens.ExplorerScreen
import com.file.material.ui.screens.HomeScreen
import com.file.material.ui.screens.SettingsScreen
import com.file.material.ui.theme.FileMaterialTheme
import com.file.material.ui.viewmodel.FileViewModel
import com.file.material.utils.FileUtils
import kotlinx.coroutines.launch

sealed class Screen {
    object Home : Screen()
    object Explorer : Screen()
    data class Category(val category: FileCategory) : Screen()
    object Settings : Screen()
}

class MainActivity : ComponentActivity() {

    private val viewModel: FileViewModel by viewModels()
    private var hasStoragePermission by mutableStateOf(false)

    private val requestLegacyPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        hasStoragePermission = granted
        if (granted) viewModel.refresh()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        checkStoragePermission()

        setContent {
            val dynamicColor by viewModel.dynamicColor.collectAsState()
            val darkThemeSetting by viewModel.darkTheme.collectAsState()
            val isDark = darkThemeSetting ?: isSystemInDarkTheme()

            FileMaterialTheme(
                darkTheme = isDark,
                dynamicColor = dynamicColor
            ) {
                MainAppContent(
                    viewModel = viewModel,
                    hasStoragePermission = hasStoragePermission,
                    onRequestPermission = { requestStoragePermission() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkStoragePermission()
    }

    private fun checkStoragePermission() {
        hasStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        } else {
            requestLegacyPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    viewModel: FileViewModel,
    hasStoragePermission: Boolean,
    onRequestPermission: () -> Unit
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    // Dialog & Sheet States
    var activeActionItem by remember { mutableStateOf<FileItem?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var renameTargetItem by remember { mutableStateOf<FileItem?>(null) }
    var deleteTargetItem by remember { mutableStateOf<FileItem?>(null) }
    var showDeleteBatchDialog by remember { mutableStateOf(false) }
    var propertiesTargetItem by remember { mutableStateOf<FileItem?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentScreen !is Screen.Category) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.clip(CircleShape)
                ) {
                    NavigationBarItem(
                        selected = currentScreen is Screen.Home,
                        onClick = { currentScreen = Screen.Home },
                        icon = { Icon(Icons.Rounded.Home, contentDescription = stringResource(R.string.nav_home)) },
                        label = { Text(stringResource(R.string.nav_home)) }
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.Explorer,
                        onClick = { currentScreen = Screen.Explorer },
                        icon = { Icon(Icons.Rounded.Folder, contentDescription = stringResource(R.string.nav_browse)) },
                        label = { Text(stringResource(R.string.nav_browse)) }
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.Settings,
                        onClick = { currentScreen = Screen.Settings },
                        icon = { Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.nav_settings)) },
                        label = { Text(stringResource(R.string.nav_settings)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                is Screen.Home -> {
                    HomeScreen(
                        viewModel = viewModel,
                        hasStoragePermission = hasStoragePermission,
                        onRequestPermission = onRequestPermission,
                        onNavigateToExplorer = { currentScreen = Screen.Explorer },
                        onNavigateToCategory = { category ->
                            viewModel.loadCategory(category)
                            currentScreen = Screen.Category(category)
                        },
                        onNavigateToSettings = { currentScreen = Screen.Settings },
                        onFileClick = { item ->
                            if (item.isDirectory) {
                                viewModel.navigateTo(item.file)
                                currentScreen = Screen.Explorer
                            } else {
                                FileUtils.openFile(context, item.file)
                            }
                        },
                        onFileMoreClick = { item ->
                            activeActionItem = item
                        }
                    )
                }
                is Screen.Explorer -> {
                    ExplorerScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.Home },
                        onFileClick = { item ->
                            if (item.isDirectory) {
                                viewModel.navigateTo(item.file)
                            } else {
                                FileUtils.openFile(context, item.file)
                            }
                        },
                        onFileMoreClick = { item ->
                            activeActionItem = item
                        },
                        onCreateFolderClick = { showCreateFolderDialog = true },
                        onDeleteSelectedClick = { showDeleteBatchDialog = true },
                        onShareSelectedClick = { items ->
                            FileUtils.shareFiles(context, items.map { it.file })
                        }
                    )
                }
                is Screen.Category -> {
                    CategoryScreen(
                        category = screen.category,
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.Home },
                        onFileClick = { item ->
                            if (item.isDirectory) {
                                viewModel.navigateTo(item.file)
                                currentScreen = Screen.Explorer
                            } else {
                                FileUtils.openFile(context, item.file)
                            }
                        },
                        onFileMoreClick = { item ->
                            activeActionItem = item
                        }
                    )
                }
                is Screen.Settings -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.Home }
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for Item Actions
    if (activeActionItem != null) {
        FileActionSheet(
            item = activeActionItem,
            sheetState = sheetState,
            onDismiss = { activeActionItem = null },
            onOpen = { item -> FileUtils.openFile(context, item.file) },
            onShare = { item -> FileUtils.shareFiles(context, listOf(item.file)) },
            onRename = { item -> renameTargetItem = item },
            onDelete = { item -> deleteTargetItem = item },
            onProperties = { item -> propertiesTargetItem = item }
        )
    }

    // Dialog: Create Folder
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { name ->
                showCreateFolderDialog = false
                viewModel.createFolder(name) { success ->
                    val msg = if (success) "Folder created" else "Could not create folder"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Dialog: Rename
    if (renameTargetItem != null) {
        RenameDialog(
            currentName = renameTargetItem!!.name,
            onDismiss = { renameTargetItem = null },
            onConfirm = { newName ->
                val target = renameTargetItem!!.file
                renameTargetItem = null
                viewModel.renameFile(target, newName) { success ->
                    val msg = if (success) "Renamed successfully" else "Failed to rename"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Dialog: Delete Single Item
    if (deleteTargetItem != null) {
        DeleteConfirmDialog(
            itemName = deleteTargetItem!!.name,
            onDismiss = { deleteTargetItem = null },
            onConfirm = {
                val target = deleteTargetItem!!.file
                deleteTargetItem = null
                viewModel.deleteFile(target) { success ->
                    val msg = if (success) "Deleted" else "Failed to delete"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Dialog: Delete Batch
    if (showDeleteBatchDialog) {
        val count = viewModel.selectedFiles.collectAsState().value.size
        DeleteConfirmDialog(
            itemName = "",
            isMultiple = true,
            itemCount = count,
            onDismiss = { showDeleteBatchDialog = false },
            onConfirm = {
                showDeleteBatchDialog = false
                viewModel.deleteSelected { success ->
                    val msg = if (success) "Deleted $count items" else "Some items could not be deleted"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Dialog: Properties
    if (propertiesTargetItem != null) {
        FilePropertiesDialog(
            item = propertiesTargetItem!!,
            onDismiss = { propertiesTargetItem = null }
        )
    }
}
