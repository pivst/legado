package io.legado.app.service

/** Original chapter offsets, including paragraph separators; never add synthetic newlines. */
internal data class ReadAloudTextChunk(val text: String, val start: Int) {
    val end: Int get() = start + text.length
}

internal const val DEFAULT_READ_ALOUD_TARGET = 1200

internal fun normalizeReadAloudTarget(value: Int): Int = value.coerceIn(100, 1200)

/**
 * Merge across screen pages. Prefer a paragraph, then a sentence boundary within 75–125%
 * of the target. Long paragraphs fall back to a clause boundary or a Unicode-safe cut.
 * Concatenating the chunks always reproduces the original chapter, byte for byte.
 */
internal fun splitReadAloudText(text: String, target: Int = DEFAULT_READ_ALOUD_TARGET): List<ReadAloudTextChunk> {
    // 1200 selects chapter-aware mode; fixed targets retain their old boundaries.
    val automatic = target == DEFAULT_READ_ALOUD_TARGET
    if (automatic && text.length <= 1200) return if (text.isEmpty()) emptyList() else listOf(ReadAloudTextChunk(text, 0))
    val parts = (text.length + 1199) / 1200
    val size = if (automatic) (text.length + parts - 1) / parts else normalizeReadAloudTarget(target)
    val minimum = size * 3 / 4
    val maximum = size * 5 / 4
    val result = ArrayList<ReadAloudTextChunk>()
    var start = 0
    while (start < text.length) {
        if (text.length - start <= maximum) {
            result.add(ReadAloudTextChunk(text.substring(start), start))
            break
        }
        val lower = start + minimum
        val upper = minOf(start + maximum, text.length)
        val preferred = start + size
        fun boundary(characters: String): Int? = (lower..upper)
            .filter { text[it - 1] in characters }
            // Keep closing quotes with their sentence and avoid splitting CRLF.
            .map { candidate ->
                var end = candidate
                while (end < upper && text[end] in "”’\"』」）)]\r\n") end++
                end
            }
            .minByOrNull { kotlin.math.abs(it - preferred) }
        var end = boundary("\n") ?: boundary("。！？.!?；;…")
            ?: boundary("，,：:、 ") ?: preferred
        if (end < text.length && Character.isHighSurrogate(text[end - 1]) &&
            Character.isLowSurrogate(text[end])) end--
        result.add(ReadAloudTextChunk(text.substring(start, end), start))
        start = end
    }
    return result
}

internal fun List<ReadAloudTextChunk>.chunkIndexAt(position: Int): Int =
    indexOfLast { it.start <= position }.coerceAtLeast(0)

/** HTTP audio has no word timestamps: interpolate only the actually requested suffix. */
internal fun httpTtsProgressOffset(length: Int, skipped: Int, positionMs: Long, durationMs: Long): Int {
    val offset = skipped.coerceIn(0, length)
    if (durationMs <= 0) return offset
    return offset + ((length - offset).toLong() * positionMs.coerceIn(0, durationMs) / durationMs).toInt()
}
