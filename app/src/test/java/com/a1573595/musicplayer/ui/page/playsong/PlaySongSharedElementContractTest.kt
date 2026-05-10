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

        assertThat(sourceTransitions)
            .containsExactly(
                "imgDisc",
                "imgDisc",
                "btn_play",
                "imgPlay"
            )
        assertThat(portraitTransitions)
            .containsExactly(
                "imgDisc",
                "imgDisc",
                "imgPlay",
                "imgPlay"
            )
        assertThat(landscapeTransitions)
            .containsExactly(
                "imgDisc",
                "imgDisc",
                "imgPlay",
                "imgPlay"
            )

        assertThat(songListActivity).contains("Pair.create(viewBinding.imgDisc")
        assertThat(songListActivity).contains("Pair.create(viewBinding.btnPlay")
        assertThat(songListActivity)
            .contains("startActivity(Intent(this, PlaySongActivity::class.java)")
        assertThat(songListActivity).doesNotContain("Pair.create(viewBinding.tvName")
        assertThat(songListActivity).doesNotContain("viewBinding.tvName.transitionName")
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
}
