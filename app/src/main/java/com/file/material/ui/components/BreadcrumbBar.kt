package com.file.material.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.file.material.ui.theme.ChipShapeExpressive
import java.io.File

@Composable
fun BreadcrumbBar(
    breadcrumbs: List<File>,
    currentDirectory: File,
    onNavigate: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(breadcrumbs.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        breadcrumbs.forEachIndexed { index, file ->
            val isLast = index == breadcrumbs.lastIndex
            val displayName = if (index == 0) "Internal Storage" else file.name

            val isDark = androidx.compose.foundation.isSystemInDarkTheme()

            AssistChip(
                onClick = { onNavigate(file) },
                label = {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                leadingIcon = if (index == 0) {
                    {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null,
                shape = ChipShapeExpressive,
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isLast) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.85f else 0.80f)
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.60f else 0.70f)
                    },
                    labelColor = if (isLast) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    androidx.compose.ui.graphics.Color.White.copy(alpha = if (isDark) 0.16f else 0.35f)
                )
            )

            if (!isLast) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}
