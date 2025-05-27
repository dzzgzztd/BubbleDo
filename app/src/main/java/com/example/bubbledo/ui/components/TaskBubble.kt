package com.example.bubbledo.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bubbledo.R
import com.example.bubbledo.model.Task
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.remember

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskBubble(
    task: Task,
    position: Offset,
    radius: Float,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val size by animateFloatAsState(targetValue = radius * 2)

    val maxFontSize = 24.sp
    val minFontSize = 12.sp

    val fontSize = remember(task.importance) {
        val importance = task.importance.coerceIn(1, 7)
        (minFontSize.value + ((importance - 1) / 6f) * (maxFontSize.value - minFontSize.value)).sp
    }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (position.x - radius).dp.roundToPx(),
                    (position.y - radius).dp.roundToPx()
                )
            }
            .size(size.dp)
            .clip(CircleShape)
            .combinedClickable(
                onClick = { onClick?.invoke() },
                onLongClick = { onLongClick?.invoke() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.bubble),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
        ) {
            Text(
                text = task.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = fontSize,
                color = Color.Black,
                lineHeight = (fontSize.value * 1.2f).sp,
                textAlign = TextAlign.Center,
            )
            task.deadline?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = java.text.SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(it)),
                    fontSize = (fontSize.value * 0.75f).sp,
                    color = Color.DarkGray,
                    lineHeight = (fontSize.value * 0.75f * 1.2f).sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
