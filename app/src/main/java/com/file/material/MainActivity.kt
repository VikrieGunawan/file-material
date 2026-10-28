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
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBarDefaults
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.file.material.data.model.FileCategory
import com.file.material.data.model.FileItem
import com.file.material.ui.components.CreateFolderDialog
import com.file.material.ui.components.DeleteConfirmDialog
import com.file.material.ui.components.FileActionSheet
import com.file.material.ui.components.FilePropertiesDialog
import com.file.material.ui.components.OpenWithBottomSheet
import com.file.material.ui.components.RenameDialog
import com.file.material.ui.components.RestrictedFolderDialog
import com.file.material.ui.screens.CategoryScreen
import com.file.material.ui.screens.ExplorerScreen
import com.file.material.ui.screens.HomeScreen
import com.file.material.ui.screens.SettingsScreen
import com.file.material.ui.theme.FileMaterialTheme
import com.file.material.ui.viewmodel.FileViewModel
import com.file.material.utils.FileUtils
import kotlinx.coroutines.launch
import java.io.File

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
            val amoledDark by viewModel.amoledDark.collectAsState()
            val isDark = darkThemeSetting ?: isSystemInDarkTheme()

            FileMaterialTheme(
                darkTheme = isDark,
                dynamicColor = dynamicColor,
                amoledDark = amoledDark
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
    var isPopping by remember { mutableStateOf(false) }

    // BackStack navigation: Back returns to previously opened screen instead of closing app
    val backStack = remember { mutableStateListOf<Screen>(Screen.Home) }
    val currentScreen = backStack.lastOrNull() ?: Screen.Home

    fun navigateTo(screen: Screen) {
        if (backStack.lastOrNull() != screen) {
            isPopping = false
            backStack.add(screen)
        }
    }

    fun popBack(): Boolean {
        if (backStack.size > 1) {
            isPopping = true
            backStack.removeAt(backStack.lastIndex)
            return true
        }
        return false
    }

    BackHandler(enabled = backStack.size > 1) {
        popBack()
    }

    // Modal Bottom Sheet & Dialog States
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Action Menu BottomSheet with guaranteed smooth animation
    val actionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showActionSheet by remember { mutableStateOf(false) }
    var activeActionItem by remember { mutableStateOf<FileItem?>(null) }

    fun openActionMenu(item: FileItem) {
        activeActionItem = item
        showActionSheet = true
    }

    fun closeActionMenu() {
        coroutineScope.launch {
            actionSheetState.hide()
        }.invokeOnCompletion {
            if (!actionSheetState.isVisible) {
                showActionSheet = false
                activeActionItem = null
            }
        }
    }

    // Custom "Open With" In-App Chooser BottomSheet
    val openWithSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showOpenWithSheet by remember { mutableStateOf(false) }
    var openWithTargetItem by remember { mutableStateOf<FileItem?>(null) }

    fun openCustomOpenWith(item: FileItem) {
        openWithTargetItem = item
        showOpenWithSheet = true
    }

    fun closeCustomOpenWith() {
        coroutineScope.launch {
            openWithSheetState.hide()
        }.invokeOnCompletion {
            if (!openWithSheetState.isVisible) {
                showOpenWithSheet = false
                openWithTargetItem = null
            }
        }
    }

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var renameTargetItem by remember { mutableStateOf<FileItem?>(null) }
    var deleteTargetItem by remember { mutableStateOf<FileItem?>(null) }
    var showDeleteBatchDialog by remember { mutableStateOf(false) }
    var propertiesTargetItem by remember { mutableStateOf<FileItem?>(null) }
    var restrictedFolderTarget by remember { mutableStateOf<File?>(null) }

    val confirmDeleteSetting by viewModel.confirmDelete.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            if (currentScreen !is Screen.Category) {
                // Navigation bar with standard system navigation insets (proper breathing room away from gesture bar)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 0.dp,
                    windowInsets = NavigationBarDefaults.windowInsets
                ) {
                    NavigationBarItem(
                        selected = currentScreen is Screen.Home,
                        onClick = {
                            if (currentScreen !is Screen.Home) navigateTo(Screen.Home)
                        },
                        icon = { Icon(Icons.Rounded.Home, contentDescription = stringResource(R.string.nav_home)) },
                        label = { Text(stringResource(R.string.nav_home)) }
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.Explorer,
                        onClick = {
                            if (currentScreen !is Screen.Explorer) navigateTo(Screen.Explorer)
                        },
                        icon = { Icon(Icons.Rounded.Folder, contentDescription = stringResource(R.string.nav_browse)) },
                        label = { Text(stringResource(R.string.nav_browse)) }
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.Settings,
                        onClick = {
                            if (currentScreen !is Screen.Settings) navigateTo(Screen.Settings)
                        },
                        icon = { Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.nav_settings)) },
                        label = { Text(stringResource(R.string.nav_settings)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                val goingBack = isPopping
                (slideInHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    initialOffsetX = { fullWidth -> if (goingBack) -fullWidth else fullWidth }
                ) + fadeIn(animationSpec = tween(220))).togetherWith(
                    slideOutHorizontally(
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        targetOffsetX = { fullWidth -> if (goingBack) fullWidth else -fullWidth }
                    ) + fadeOut(animationSpec = tween(180))
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                is Screen.Home -> {
                    HomeScreen(
                        viewModel = viewModel,
                        hasStoragePermission = hasStoragePermission,
                        onRequestPermission = onRequestPermission,
                        onNavigateToExplorer = { navigateTo(Screen.Explorer) },
                        onNavigateToCategory = { category ->
                            viewModel.loadCategory(category)
                            navigateTo(Screen.Category(category))
                        },
                        onNavigateToSettings = { navigateTo(Screen.Settings) },
                        onFileClick = { item ->
                            if (item.isDirectory) {
                                viewModel.navigateTo(item.file)
                                navigateTo(Screen.Explorer)
                            } else {
                                openCustomOpenWith(item)
                            }
                        },
                        onFileMoreClick = { item ->
                            openActionMenu(item)
                        }
                    )
                }
                is Screen.Explorer -> {
                    ExplorerScreen(
                        viewModel = viewModel,
                        onNavigateBack = { popBack() },
                        onFileClick = { item ->
                            if (item.isDirectory) {
                                viewModel.navigateTo(item.file)
                            } else {
                                openCustomOpenWith(item)
                            }
                        },
                        onFileMoreClick = { item ->
                            openActionMenu(item)
                        },
                        onCreateFolderClick = { showCreateFolderDialog = true },
                        onDeleteSelectedClick = {
                            if (confirmDeleteSetting) {
                                showDeleteBatchDialog = true
                            } else {
                                val count = viewModel.selectedFiles.value.size
                                viewModel.deleteSelected { success ->
                                    val msg = if (success) "Deleted $count items" else "Could not delete some items"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onShareSelectedClick = { items ->
                            FileUtils.shareFiles(context, items.map { it.file })
                        },
                        onRestrictedFolder = { folder ->
                            restrictedFolderTarget = folder
                        }
                    )
                }
                is Screen.Category -> {
                    CategoryScreen(
                        category = screen.category,
                        viewModel = viewModel,
                        onNavigateBack = { popBack() },
                        onFileClick = { item ->
                            if (item.isDirectory) {
                                viewModel.navigateTo(item.file)
                                navigateTo(Screen.Explorer)
                            } else {
                                openCustomOpenWith(item)
                            }
                        },
                        onFileMoreClick = { item ->
                            openActionMenu(item)
                        }
                    )
                }
                is Screen.Settings -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        hasStoragePermission = hasStoragePermission,
                        onNavigateBack = { popBack() }
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for Item Actions with smooth animated transitions
    if (showActionSheet && activeActionItem != null) {
        FileActionSheet(
            item = activeActionItem,
            sheetState = actionSheetState,
            onDismiss = { closeActionMenu() },
            onOpen = { item ->
                closeActionMenu()
                openCustomOpenWith(item)
            },
            onShare = { item -> FileUtils.shareFiles(context, listOf(item.file)) },
            onRename = { item -> renameTargetItem = item },
            onDelete = { item ->
                if (confirmDeleteSetting) {
                    deleteTargetItem = item
                } else {
                    viewModel.deleteFile(item.file) { success ->
                        val msg = if (success) "Deleted" else "Failed to delete"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onProperties = { item -> propertiesTargetItem = item }
        )
    }

    // Custom Open With in-app Bottom Sheet (avoiding Android duplicate dialogs)
    if (showOpenWithSheet && openWithTargetItem != null) {
        OpenWithBottomSheet(
            item = openWithTargetItem,
            sheetState = openWithSheetState,
            onDismiss = { closeCustomOpenWith() }
        )
    }

    // Dialog: Restricted Android Folder (Android/data or Android/obb)
    if (restrictedFolderTarget != null) {
        RestrictedFolderDialog(
            folder = restrictedFolderTarget!!,
            onDismiss = { restrictedFolderTarget = null }
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

    // Dialog: Properties (Completely overhauled UI with icons and copy buttons)
    if (propertiesTargetItem != null) {
        FilePropertiesDialog(
            item = propertiesTargetItem!!,
            onDismiss = { propertiesTargetItem = null }
        )
    }
}
