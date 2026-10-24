package com.file.material.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.file.material.R
import com.file.material.data.model.FileItem
import com.file.material.ui.theme.ButtonShapeExpressive
import com.file.material.ui.theme.DialogShapeExpressive
import com.file.material.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = DialogShapeExpressive,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.dialog_properties_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                PropertyRow(label = "Name", value = item.name)
                PropertyRow(label = "Location", value = item.path)
                PropertyRow(
                    label = "Size",
                    value = if (item.isDirectory) "${item.childCount} items" else "${FileUtils.formatSize(item.size)} (${item.size} bytes)"
                )
                PropertyRow(label = "Type", value = if (item.isDirectory) "Folder" else item.mimeType.ifBlank { item.extension.uppercase() })
                PropertyRow(label = "Modified", value = FileUtils.formatDate(item.lastModified))
                PropertyRow(
                    label = "Permissions",
                    value = buildString {
                        append(if (item.file.canRead()) "R" else "-")
                        append(if (item.file.canWrite()) "W" else "-")
                        append(if (item.file.canExecute()) "X" else "-")
                    }
                )
                if (!item.isDirectory) {
                    PropertyRow(label = "MD5 Hash", value = md5Hash)
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
private fun PropertyRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
