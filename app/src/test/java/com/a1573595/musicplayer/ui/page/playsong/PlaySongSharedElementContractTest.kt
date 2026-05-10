package com.a1573595.musicplayer.ui.page.playsong

import com.google.common.truth.Truth.assertThat
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Test
import org.w3c.dom.Element

class PlaySongSharedElementContractTest {
    @Test
    fun songListToPlaySong_usesOnlyDiscAndPlaySharedElements() {
        val sourceTransitions = transitionNamesById("app/src/main/res/layout/activity_song_list.xml")
        val portraitTransitions = transitionNamesById("app/src/main/res/layout/activity_play_song.xml")
        val landscapeTransitions =
            transitionNamesById("app/src/main/res/layout-land/activity_play_song.xml")
        val songListActivity =
            projectFile(
                "app/src/main/java/com/a1573595/musicplayer/ui/page/songlist/SongListActivityBase.kt"
            ).readText()
        val bottomAppBarClick = songListActivity.bottomAppBarClickBlock()

        assertThat(sourceTransitions)
            .containsExactly(
                "imgDisc",
                "imgDisc",
                "btn_play",
                "imgPlay"
            )
        assertThat(sourceTransitions.keys).containsExactly("imgDisc", "btn_play")
        assertThat(sourceTransitions).doesNotContainKey("tvName")
        assertThat(portraitTransitions)
            .containsExactly(
                "imgDisc",
                "imgDisc",
                "imgPlay",
                "imgPlay"
            )
        assertThat(portraitTransitions.keys).containsExactly("imgDisc", "imgPlay")
        assertThat(portraitTransitions).doesNotContainKey("tvName")
        assertThat(portraitTransitions).doesNotContainKey("tvProgress")
        assertThat(portraitTransitions).doesNotContainKey("seekBar")
        assertThat(landscapeTransitions)
            .containsExactly(
                "imgDisc",
                "imgDisc",
                "imgPlay",
                "imgPlay"
            )
        assertThat(landscapeTransitions.keys).containsExactly("imgDisc", "imgPlay")
        assertThat(landscapeTransitions).doesNotContainKey("tvName")
        assertThat(landscapeTransitions).doesNotContainKey("tvProgress")
        assertThat(landscapeTransitions).doesNotContainKey("seekBar")

        assertThat(bottomAppBarClick.sharedElementBindingNames())
            .containsExactly("imgDisc", "btnPlay")
            .inOrder()
        assertThat(bottomAppBarClick).contains("Pair.create(viewBinding.imgDisc")
        assertThat(bottomAppBarClick).contains("Pair.create(viewBinding.btnPlay")
        assertThat(bottomAppBarClick)
            .contains("startActivity(Intent(this, PlaySongActivity::class.java)")
        assertThat(bottomAppBarClick).contains("if (bottomMiniPlayerState.value.hasSong)")
        assertThat(bottomAppBarClick.indexOf("if (bottomMiniPlayerState.value.hasSong)"))
            .isLessThan(bottomAppBarClick.indexOf("startActivity("))
        assertThat(bottomAppBarClick).doesNotContain("Pair.create(viewBinding.tvName")
        assertThat(bottomAppBarClick).doesNotContain("viewBinding.tvName.transitionName")
    }

    private fun transitionNamesById(path: String): Map<String, String> {
        val document =
            DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(projectFile(path))

        val transitions = mutableMapOf<String, String>()
        document.documentElement.collectTransitionNames(transitions)
        return transitions
    }

    private fun Element.collectTransitionNames(transitions: MutableMap<String, String>) {
        val id = getAttribute("android:id").substringAfter("/", missingDelimiterValue = "")
        val transitionName = getAttribute("android:transitionName")

        if (id.isNotEmpty() && transitionName.isNotEmpty()) {
            transitions[id] = resolveStringResource(transitionName)
        }

        for (index in 0 until childNodes.length) {
            (childNodes.item(index) as? Element)?.collectTransitionNames(transitions)
        }
    }

    private fun resolveStringResource(value: String): String {
        if (!value.startsWith("@string/")) return value

        val name = value.substringAfter("@string/")
        return stringResources()[name] ?: error("Cannot resolve string resource: $value")
    }

    private fun stringResources(): Map<String, String> {
        val document =
            DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(projectFile("app/src/main/res/values/strings.xml"))
        val strings = mutableMapOf<String, String>()
        val nodes = document.getElementsByTagName("string")

        for (index in 0 until nodes.length) {
            val element = nodes.item(index) as Element
            strings[element.getAttribute("name")] = element.textContent
        }

        return strings
    }

    private fun projectFile(path: String): File {
        val currentDir = File(System.getProperty("user.dir") ?: error("user.dir is missing"))
        val parentDir = currentDir.parentFile ?: currentDir
        val candidates =
            listOf(
                File(currentDir, path),
                File(currentDir, path.removePrefix("app/")),
                File(parentDir, path)
            )

        return candidates.firstOrNull { it.isFile }
            ?: error("Cannot find project file: $path from ${currentDir.absolutePath}")
    }

    private fun String.bottomAppBarClickBlock(): String {
        val startMarker = "viewBinding.bottomAppBar.setOnClickListener"
        val endMarker = "private fun onSearchQueryChange"
        val startIndex = indexOf(startMarker)
        check(startIndex >= 0) { "Cannot find bottomAppBar click listener" }

        val endIndex = indexOf(endMarker, startIndex)
        check(endIndex >= 0) { "Cannot find end of bottomAppBar click listener" }

        return substring(startIndex, endIndex)
    }

    private fun String.sharedElementBindingNames(): List<String> =
        Regex("""Pair\.create\(viewBinding\.(\w+)""")
            .findAll(this)
            .map { it.groupValues[1] }
            .toList()
}
