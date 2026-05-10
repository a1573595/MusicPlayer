package com.a1573595.musicplayer.ui.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.a1573595.musicplayer.R

@Composable
fun MusicPlayerComposeTheme(content: @Composable () -> Unit) {
    val colorScheme =
        lightColorScheme(
            primary = colorResource(id = R.color.colorPrimary),
            secondary = colorResource(id = R.color.colorAccent),
            background = colorResource(id = android.R.color.white),
            surface = colorResource(id = R.color.colorCardBackground),
            onPrimary = colorResource(id = R.color.colorTitleWhite),
            onSecondary = colorResource(id = R.color.colorTitleWhite),
            onBackground = colorResource(id = R.color.colorTitleBlack),
            onSurface = colorResource(id = R.color.colorTitleBlack)
        )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
