package com.file.material.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.file.material.data.model.FileItem
import com.file.material.ui.theme.CardShapeExpressive
import com.file.material.ui.theme.IconContainerShapeExpressive
import com.file.material.utils.FileUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileGridItem(
    item: FileItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, tint, bgColor) = getFileVisuals(item)

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShapeExpressive)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = CardShapeExpressive,
        tintColor = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        } else null,
        elevation = if (isSelected) 6.dp else 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FileThumbnail(
                        item = item,
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        iconSize = 32.dp,
                        fallbackSize = 56.dp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (item.isDirectory) {
                        "${item.childCount} items"
                    } else {
                        FileUtils.formatSize(item.size)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
