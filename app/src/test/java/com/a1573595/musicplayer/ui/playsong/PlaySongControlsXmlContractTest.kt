package com.a1573595.musicplayer.ui.playsong

import com.google.common.truth.Truth.assertThat
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Test
import org.w3c.dom.Element

class PlaySongControlsXmlContractTest {
    @Test
    fun portraitControls_keepCurrentXmlContract() {
        assertControlsContract("app/src/main/res/layout/activity_play_song.xml")
    }

    @Test
    fun landscapeControls_keepCurrentXmlContract() {
        assertControlsContract("app/src/main/res/layout-land/activity_play_song.xml")
    }

    private fun assertControlsContract(path: String) {
        val controls = elementsById(path)

        assertThat(controls.getValue("imgRepeat").getAttribute("app:srcCompat"))
            .isEqualTo("@drawable/ic_repeat")
        assertThat(controls.getValue("imgRandom").getAttribute("app:srcCompat"))
            .isEqualTo("@drawable/ic_random")
        assertThat(controls.getValue("imgBackward").getAttribute("app:srcCompat"))
            .isEqualTo("@drawable/ic_next")
        assertThat(controls.getValue("imgBackward").getAttribute("android:rotation"))
            .isEqualTo("180")
        assertThat(controls.getValue("imgPlay").getAttribute("app:srcCompat"))
            .isEqualTo("@drawable/selector_play_pause")
        assertThat(controls.getValue("imgPlay").getAttribute("android:transitionName"))
            .isEqualTo("@string/transition_img_play")
        assertThat(controls.getValue("imgForward").getAttribute("app:srcCompat"))
            .isEqualTo("@drawable/ic_next")
        assertThat(controls.getValue("tvName").getAttribute("android:transitionName"))
            .isEmpty()
    }

    private fun elementsById(path: String): Map<String, Element> {
        val document =
            DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(projectFile(path))
        val elements = mutableMapOf<String, Element>()

        document.documentElement.collectElementsById(elements)
        return elements
    }

    private fun Element.collectElementsById(elements: MutableMap<String, Element>) {
        val id = getAttribute("android:id").substringAfter("/", missingDelimiterValue = "")

        if (id.isNotEmpty()) {
            elements[id] = this
        }

        for (index in 0 until childNodes.length) {
            (childNodes.item(index) as? Element)?.collectElementsById(elements)
        }
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
