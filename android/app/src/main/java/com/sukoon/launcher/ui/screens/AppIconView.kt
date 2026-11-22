package com.sukoon.launcher.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sukoon.launcher.ui.theme.LocalSukoonColors

@Composable
fun AppIconView(
    packageName: String,
    displayStyle: String,
    size: Dp = 36.dp,
    loadIcon: suspend (String) -> ImageBitmap?
) {
    if (displayStyle == "text") return

    val colors = LocalSukoonColors.current
    var bitmap by remember(packageName) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(packageName, displayStyle) {
        if (bitmap == null) {
            bitmap = loadIcon(packageName)
        }
    }

    val isMono = displayStyle == "monochrome"
    val monoFilter = remember {
        ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    }
    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(if (isMono) colors.surface else colors.surfaceHover)
            .then(
                if (isMono) Modifier.border(1.dp, colors.borderSubtle, shape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let { img ->
            Image(
                bitmap = img,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = if (isMono) monoFilter else null,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isMono) Modifier.padding(4.dp).alpha(0.86f) else Modifier)
            )
        }
    }
}
