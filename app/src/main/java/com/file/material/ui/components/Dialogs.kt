package com.file.material.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Launch
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Storage
import com.file.material.utils.PrivilegedServiceUtils
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.file.material.R
import com.file.material.data.model.FileItem
import com.file.material.ui.theme.ButtonShapeExpressive
import com.file.material.ui.theme.DialogShapeExpressive
import com.file.material.ui.theme.IconContainerShapeExpressive
import com.file.material.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DialogShapeExpressive,
        icon = {
            Icon(
                imageVector = Icons.Rounded.CreateNewFolder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.dialog_create_folder_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = {
                        folderName = it
                        isError = it.isBlank() || it.contains("/")
                    },
                    label = { Text(stringResource(R.string.dialog_create_folder_hint)) },
                    singleLine = true,
                    isError = isError,
                    shape = ButtonShapeExpressive,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (folderName.isNotBlank() && !folderName.contains("/")) {
                        onConfirm(folderName.trim())
                    } else {
                        isError = true
                    }
                },
                shape = ButtonShapeExpressive
            ) {
                Text(stringResource(R.string.action_create))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = ButtonShapeExpressive
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DialogShapeExpressive,
        icon = {
            Icon(
                imageVector = Icons.Rounded.DriveFileRenameOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.dialog_rename_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = newName,
                    onValueChange = {
                        newName = it
                        isError = it.isBlank() || it.contains("/")
                    },
                    label = { Text(stringResource(R.string.dialog_rename_hint)) },
                    singleLine = true,
                    isError = isError,
                    shape = ButtonShapeExpressive,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newName.isNotBlank() && !newName.contains("/")) {
                        onConfirm(newName.trim())
                    } else {
                        isError = true
                    }
                },
                shape = ButtonShapeExpressive
            ) {
                Text(stringResource(R.string.action_apply))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = ButtonShapeExpressive
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    itemName: String,
    isMultiple: Boolean = false,
    itemCount: Int = 1,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DialogShapeExpressive,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = stringResource(R.string.dialog_delete_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = if (isMultiple) {
                    stringResource(R.string.dialog_delete_batch_message, itemCount)
                } else {
                    stringResource(R.string.dialog_delete_message, itemName)
                },
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = ButtonShapeExpressive,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(stringResource(R.string.action_delete))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = ButtonShapeExpressive
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
fun FilePropertiesDialog(
    item: FileItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var md5Hash by remember { mutableStateOf("Calculating...") }

    LaunchedEffect(item) {
        if (!item.isDirectory) {
            withContext(Dispatchers.IO) {
                md5Hash = FileUtils.calculateMD5(item.file)
            }
        } else {
            md5Hash = "Directory (N/A)"
        }
    }

    val (icon, tint, bgColor) = getFileVisuals(item)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DialogShapeExpressive,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(IconContainerShapeExpressive)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (item.isDirectory) "Folder · ${item.childCount} items" else "${FileUtils.formatSize(item.size)} · ${item.extension.uppercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Location / Path
                PropertyItemCard(
                    icon = Icons.Rounded.Folder,
                    label = "Location",
                    value = item.path,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(item.path))
                        Toast.makeText(context, "Path copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )

                // Size
                PropertyItemCard(
                    icon = Icons.Rounded.Storage,
                    label = "Size",
                    value = if (item.isDirectory) "${item.childCount} items inside" else "${FileUtils.formatSize(item.size)} (${item.size} bytes)"
                )

                // Date Modified
                PropertyItemCard(
                    icon = Icons.Rounded.CalendarToday,
                    label = "Last Modified",
                    value = FileUtils.formatDate(item.lastModified)
                )

                // MIME Type
                PropertyItemCard(
                    icon = Icons.Rounded.Category,
                    label = "Type",
                    value = if (item.isDirectory) "Folder" else item.mimeType.ifBlank { item.extension.uppercase() }
                )

                // Access Permissions
                PropertyItemCard(
                    icon = Icons.Rounded.Security,
                    label = "Access Rights",
                    value = buildString {
                        append(if (item.file.canRead()) "Read: Yes" else "Read: No")
                        append(" · ")
                        append(if (item.file.canWrite()) "Write: Yes" else "Write: No")
                    }
                )

                // MD5 Hash (for files)
                if (!item.isDirectory) {
                    PropertyItemCard(
                        icon = Icons.Rounded.Fingerprint,
                        label = "MD5 Hash",
                        value = md5Hash,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(md5Hash))
                            Toast.makeText(context, "MD5 copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = ButtonShapeExpressive
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun PropertyItemCard(
    icon: ImageVector,
    label: String,
    value: String,
    onCopy: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (onCopy != null) {
                IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RestrictedFolderDialog(
    folder: File,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val privilegedApps = remember { PrivilegedServiceUtils.getPrivilegedApps(context) }
    val installedApp = remember { privilegedApps.firstOrNull { it.isInstalled } }
    val isRoot = remember { PrivilegedServiceUtils.isRootAvailable() }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DialogShapeExpressive,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        title = {
            Text(
                text = "Access Restricted by Android",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Android 11+ strictly blocks file managers from directly accessing \"${folder.name}\" (/Android/${folder.name}) to protect app sandboxes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Installed Privileged Manager Detection
                if (installedApp != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Detected: ${installedApp.name}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${installedApp.name} is installed on your device. Start the service or grant privileges to unlock restricted folders.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    PrivilegedServiceUtils.launchApp(context, installedApp.packageName)
                                },
                                shape = ButtonShapeExpressive,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(Icons.Rounded.Launch, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open ${installedApp.name}")
                            }
                        }
                    }
                }

                // Supported Methods (Not just Wireless!)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⚡ Activation Methods (Shizuku, Stellar & Forks)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "1. Root Access (Magisk / KernelSU / APatch): One-tap start directly with superuser rights.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "2. PC ADB (USB): Connect to computer and execute the startup script via terminal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "3. Wireless ADB: On-device pairing without a computer (Android 11+).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Download options if not installed
                if (installedApp == null) {
                    Text(
                        text = "Compatible Privileged Managers:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { PrivilegedServiceUtils.openUrl(context, "https://shizuku.rikka.app") },
                            shape = ButtonShapeExpressive,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.OpenInBrowser, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Shizuku")
                        }
                        OutlinedButton(
                            onClick = { PrivilegedServiceUtils.openUrl(context, "https://github.com/a13e300/Stellar") },
                            shape = ButtonShapeExpressive,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.OpenInBrowser, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stellar")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = ButtonShapeExpressive
            ) {
                Text("Got It")
            }
        }
    )
}
