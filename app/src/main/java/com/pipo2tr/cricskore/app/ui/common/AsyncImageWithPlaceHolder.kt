package com.pipo2tr.cricskore.app.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest

@Composable
fun AsyncImageWithPlaceHolder(imgRes: String, contentDescription: String, size: Dp) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imgRes)
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCacheKey(imgRes)
            .build(),
        modifier = Modifier
            .clip(CircleShape)
            .size(size),
        contentScale = ContentScale.Crop,
        contentDescription = contentDescription,
        placeholder = rememberVectorPainter(Icons.Rounded.BrokenImage)
    )
}