package com.a1573595.musicplayer.ui.songlist

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.ui.compose.MusicPlayerComposeTheme

const val LoadingDialogContentTestTag = "loading_dialog_content"

@Composable
fun LoadingDialogContent(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 750, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
        label = "loading_rotation_degrees"
    )

    Box(
        modifier =
            modifier
                .size(160.dp)
                .testTag(LoadingDialogContentTestTag),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_loader),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            modifier =
                Modifier
                    .size(48.dp)
                    .graphicsLayer(rotationZ = rotation)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingDialogContentPreview() {
    MusicPlayerComposeTheme {
        LoadingDialogContent()
    }
}
