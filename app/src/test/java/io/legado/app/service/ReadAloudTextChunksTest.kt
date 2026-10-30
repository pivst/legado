package io.legado.app.service

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class ReadAloudTextChunksTest {
    @Test fun `smart mode keeps short chapters whole and balances longer chapters`() {
        for (length in listOf(1, 400, 1200, 1201, 2000, 2400, 2401, 10000)) {
            val text = "甲".repeat(length)
            val chunks = splitReadAloudText(text)
            assertEquals(text, chunks.joinToString("") { it.text })
            assertTrue(chunks.all { it.text.length <= 1500 })
            if (length <= 1200) assertEquals(1, chunks.size)
            if (length in 1201..2400) assertEquals(2, chunks.size)
        }
    }

    @Test
    fun `short paragraphs merge with original newlines preserved`() {
        val text = List(20) { "甲".repeat(49) + "\n" }.joinToString("")
        val chunks = splitReadAloudText(text, 400)
        assertEquals(listOf(400, 400, 200), chunks.map { it.text.length })
        assertEquals(listOf(0, 400, 800), chunks.map { it.start })
        assertEquals(text, chunks.joinToString("") { it.text })
        assertTrue(chunks.first().text.count { it == '\n' } > 1)
    }

    @Test
    fun `splitting depends on chapter text and not screen page boundaries`() {
        val text = ("短段落。\n" + "很长的正文。".repeat(80) + "\n").repeat(5)
        val expected = splitReadAloudText(text, 400)
        for (pageSize in listOf(131, 287, 613)) {
            val pages = text.chunked(pageSize)
            assertEquals(expected, splitReadAloudText(pages.joinToString(""), 400))
        }
    }

    @Test
    fun `long paragraph splits at sentence end with closing quote`() {
        val text = "甲".repeat(360) + "。”" + "乙".repeat(600)
        val chunks = splitReadAloudText(text, 400)
        assertEquals(362, chunks.first().end)
        assertTrue(chunks.first().text.endsWith("。”"))
        assertEquals(text, chunks.joinToString("") { it.text })
    }

    @Test
    fun `no punctuation still stays bounded without losing text`() {
        val text = "甲".repeat(2101)
        val chunks = splitReadAloudText(text, 400)
        assertEquals(listOf(400, 400, 400, 400, 400, 101), chunks.map { it.text.length })
        assertEquals(text, chunks.joinToString("") { it.text })
    }

    @Test
    fun `unicode surrogate pairs are never cut in half`() {
        val text = "甲".repeat(399) + "😀" + "乙".repeat(600)
        val chunks = splitReadAloudText(text, 400)
        assertEquals(399, chunks.first().end)
        chunks.forEach {
            assertFalse(Character.isHighSurrogate(it.text.last()))
            assertFalse(Character.isLowSurrogate(it.text.first()))
        }
        assertEquals(text, chunks.joinToString("") { it.text })
    }

    @Test
    fun `start within merged block sends only unread suffix and advances without extra newline`() {
        val text = ("甲".repeat(49) + "\n").repeat(20)
        val chunks = splitReadAloudText(text, 400)
        for (position in listOf(0, 50, 399, 400, 450, 799, 800, 999)) {
            val index = chunks.chunkIndexAt(position)
            val first = chunks[index]
            val suffix = first.text.substring(position - first.start) +
                chunks.drop(index + 1).joinToString("") { it.text }
            assertEquals(text.substring(position), suffix)
            if (index < chunks.lastIndex) assertEquals(first.end, chunks[index + 1].start)
        }
    }

    @Test
    fun `partial audio progress covers the suffix only and clamps invalid durations`() {
        assertEquals(120, httpTtsProgressOffset(400, 120, 0, 1000))
        assertEquals(260, httpTtsProgressOffset(400, 120, 500, 1000))
        assertEquals(400, httpTtsProgressOffset(400, 120, 1000, 1000))
        assertEquals(120, httpTtsProgressOffset(400, 120, 50, -1))
        assertEquals(400, httpTtsProgressOffset(400, 120, 9999, 1000))
    }

    @Test
    fun `empty text and short chapter flush safely`() {
        assertTrue(splitReadAloudText("", 400).isEmpty())
        assertEquals(listOf(ReadAloudTextChunk("短篇。\n", 0)), splitReadAloudText("短篇。\n", 400))
        assertEquals(100, normalizeReadAloudTarget(Int.MIN_VALUE))
        assertEquals(1200, normalizeReadAloudTarget(Int.MAX_VALUE))
    }

    @Test
    fun `random chapter chunks are lossless contiguous and below Fish limit`() {
        val random = Random(418)
        val symbols = listOf("甲", "乙", "。", "！", "？", "\n", "\r\n", "😀", "𠀀", " ”", "…")
        repeat(100) {
            val text = buildString { repeat(random.nextInt(1, 5000)) { append(symbols.random(random)) } }
            val target = listOf(100, 200, 300, 400, 500, 600, 800, 1000).random(random)
            val chunks = splitReadAloudText(text, target)
            assertEquals(text, chunks.joinToString("") { it.text })
            var end = 0
            chunks.forEach {
                assertEquals(end, it.start)
                assertTrue(it.text.isNotEmpty())
                assertTrue(it.text.length <= target * 5 / 4)
                assertTrue(it.text.length < 2000)
                assertFalse(Character.isHighSurrogate(it.text.last()))
                assertFalse(Character.isLowSurrogate(it.text.first()))
                end = it.end
            }
            assertEquals(text.length, end)
        }
    }
}
