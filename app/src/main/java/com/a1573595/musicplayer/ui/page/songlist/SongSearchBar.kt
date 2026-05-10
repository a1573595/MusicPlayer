package com.a1573595.musicplayer.ui.page.songlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.ui.compose.MusicPlayerComposeTheme

const val SongSearchBarTestTag = "song_search_bar"
const val SongSearchBarInputTestTag = "song_search_bar_input"
const val SongSearchBarHintTestTag = "song_search_bar_hint"
const val SongSearchBarInfoTestTag = "song_search_bar_info"
const val SongSearchBarSearchIconTestTag = "song_search_bar_search_icon"

@Composable
fun SongSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit = {}
) {
    val barColor = colorResource(id = R.color.colorPrimaryDark)
    val focusManager = LocalFocusManager.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxSize()
                .background(barColor)
                .padding(start = 8.dp, end = 8.dp)
                .testTag(SongSearchBarTestTag)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle =
                TextStyle(
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 18.sp
                ),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
            keyboardActions =
                KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        onSearch(query)
                    }
                ),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier =
                        Modifier
                            .size(32.dp)
                            .testTag(SongSearchBarSearchIconTestTag)
                )
            },
            placeholder = {
                Text(
                    text = stringResource(id = R.string.search_song),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag(SongSearchBarHintTestTag)
                )
            },
            colors =
                TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    cursorColor = MaterialTheme.colorScheme.onPrimary,
                    focusedContainerColor = barColor,
                    unfocusedContainerColor = barColor,
                    disabledContainerColor = barColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
            modifier =
                Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag(SongSearchBarInputTestTag)
        )

        IconButton(
            onClick = onInfoClick,
            modifier =
                Modifier
                    .size(48.dp)
                    .testTag(SongSearchBarInfoTestTag)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_info),
                contentDescription = stringResource(id = R.string.song_search_bar_info),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SongSearchBarPreview() {
    MusicPlayerComposeTheme {
        SongSearchBar(
            query = "",
            onQueryChange = {},
            onInfoClick = {}
        )
    }
}
