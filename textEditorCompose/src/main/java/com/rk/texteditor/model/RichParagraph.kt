package com.rk.texteditor.model

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.sp
import com.rk.texteditor.ui.test.getRichTextStyleTreeRepresentation
import com.rk.texteditor.model.RichParagraph.Type.Companion.startText
import com.rk.texteditor.utils.fastForEach
import com.rk.texteditor.utils.fastForEachIndexed

internal class RichParagraph(
    val key: Int = 0,
    val children: MutableList<com.rk.texteditor.model.RichSpan> = mutableListOf(),
    var paragraphStyle: ParagraphStyle = _root_ide_package_.com.rk.texteditor.model.RichParagraph.Companion.DefaultParagraphStyle,
    var type: com.rk.texteditor.model.RichParagraph.Type = _root_ide_package_.com.rk.texteditor.model.RichParagraph.Type.Default,
) {
    interface Type {
        val style: ParagraphStyle get() = ParagraphStyle()
        val startRichSpan: _root_ide_package_.com.rk.texteditor.model.RichSpan
            get() = _root_ide_package_.com.rk.texteditor.model.RichSpan(
                paragraph = _root_ide_package_.com.rk.texteditor.model.RichParagraph(type = this)
            )

        val nextParagraphType: _root_ide_package_.com.rk.texteditor.model.RichParagraph.Type get() = _root_ide_package_.com.rk.texteditor.model.RichParagraph.Type.Default

        fun copy(): _root_ide_package_.com.rk.texteditor.model.RichParagraph.Type = this

        object Default : _root_ide_package_.com.rk.texteditor.model.RichParagraph.Type

        object UnorderedList : _root_ide_package_.com.rk.texteditor.model.RichParagraph.Type {
            override val style: ParagraphStyle = ParagraphStyle(
                textIndent = TextIndent(firstLine = 20.sp),
                lineHeight = 20.sp,
            )
            override val startRichSpan: _root_ide_package_.com.rk.texteditor.model.RichSpan =
                _root_ide_package_.com.rk.texteditor.model.RichSpan(
                    paragraph = _root_ide_package_.com.rk.texteditor.model.RichParagraph(type = this),
                    text = "• ",
                )
            override val nextParagraphType: _root_ide_package_.com.rk.texteditor.model.RichParagraph.Type get() = _root_ide_package_.com.rk.texteditor.model.RichParagraph.Type.UnorderedList
        }

        data class OrderedList(
            val number: Int,
        ) : _root_ide_package_.com.rk.texteditor.model.RichParagraph.Type {
            override val style: ParagraphStyle = ParagraphStyle(
                textIndent = TextIndent(firstLine = 20.sp),
                lineHeight = 20.sp,
            )
            override val startRichSpan: _root_ide_package_.com.rk.texteditor.model.RichSpan =
                _root_ide_package_.com.rk.texteditor.model.RichSpan(
                    paragraph = _root_ide_package_.com.rk.texteditor.model.RichParagraph(type = this),
                    text = "$number. ",
                )
            override val nextParagraphType: _root_ide_package_.com.rk.texteditor.model.RichParagraph.Type
                get() = _root_ide_package_.com.rk.texteditor.model.RichParagraph.Type.OrderedList(
                    number + 1
                )

            override fun copy(): _root_ide_package_.com.rk.texteditor.model.RichParagraph.Type =
                _root_ide_package_.com.rk.texteditor.model.RichParagraph.Type.OrderedList(number)
        }

        companion object {
            val _root_ide_package_.com.rk.texteditor.model.RichParagraph.Type.startText : String get() = startRichSpan.text
        }
    }

    fun getRichSpanByTextIndex(
        paragraphIndex: Int,
        textIndex: Int,
        offset: Int = 0,
        ignoreCustomFiltering: Boolean = false,
    ): Pair<Int, _root_ide_package_.com.rk.texteditor.model.RichSpan?> {
        // If the paragraph is empty, we add a RichSpan to avoid skipping the paragraph when searching
        if (children.isEmpty()) children.add(
            _root_ide_package_.com.rk.texteditor.model.RichSpan(
                paragraph = this,
                textRange = TextRange(offset + type.startText.length),
            )
        )

        var index = offset

        // If the paragraph is not the first one, we add 1 to the index which stands for the line break
        if (paragraphIndex > 0) index++

        // Set the startRichSpan paragraph and textRange to ensure that it has the correct and latest values
        type.startRichSpan.paragraph = this
        type.startRichSpan.textRange = TextRange(index, index + type.startText.length)

        // Add the startText length to the index
        index += type.startText.length

        // Check if the textIndex is in the startRichSpan current paragraph
        if (index > textIndex) return index to getFirstNonEmptyChild(offset = index)

        children.fastForEach { richSpan ->
            val result = richSpan.getRichSpanByTextIndex(
                textIndex = textIndex,
                offset = index,
                ignoreCustomFiltering = ignoreCustomFiltering,
            )
            if (result.second != null)
                return result
            else
                index = result.first
        }
        return index to null
    }

    fun getRichSpanListByTextRange(
        paragraphIndex: Int,
        searchTextRange: TextRange,
        offset: Int = 0,
    ): Pair<Int, List<_root_ide_package_.com.rk.texteditor.model.RichSpan>> {
        // If the paragraph is empty, we add a RichSpan to avoid skipping the paragraph when searching
        if (children.isEmpty()) children.add(
            _root_ide_package_.com.rk.texteditor.model.RichSpan(
                paragraph = this
            )
        )

        var index = offset
        index += type.startText.length

        // If the paragraph is not the first one, we add 1 to the index which stands for the line break
        if (paragraphIndex > 0) index++

        val richSpanList = mutableListOf<_root_ide_package_.com.rk.texteditor.model.RichSpan>()
        children.fastForEach { richSpan ->
            val result = richSpan.getRichSpanListByTextRange(
                searchTextRange = searchTextRange,
                offset = index,
            )
            richSpanList.addAll(result.second)
            index = result.first
        }
        return index to richSpanList
    }

    fun removeTextRange(
        textRange: TextRange,
        offset: Int,
    ): _root_ide_package_.com.rk.texteditor.model.RichParagraph? {
        var index = offset
        val toRemoveIndices = mutableListOf<Int>()
        for (i in 0..children.lastIndex) {
            val child = children[i]
            val result = child.removeTextRange(textRange, index)
            val newRichSpan = result.second
            if (newRichSpan != null) {
                children[i] = newRichSpan
            } else {
                toRemoveIndices.add(i)
            }
            index = result.first
        }
        for (i in toRemoveIndices.lastIndex downTo 0) {
            children.removeAt(toRemoveIndices[i])
        }

        if (children.isEmpty()) return null
        return this
    }

    fun isEmpty(): Boolean {
        if (children.isEmpty()) return true
        children.fastForEach { richSpan ->
            if (!richSpan.isEmpty()) return false
        }
        return true
    }

    fun getFirstNonEmptyChild(offset: Int? = null): _root_ide_package_.com.rk.texteditor.model.RichSpan? {
        children.fastForEach { richSpan ->
            if (richSpan.text.isNotEmpty()) {
                if (offset != null)
                    richSpan.textRange = TextRange(offset, offset + richSpan.text.length)
                return richSpan
            }
            else {
                val result = richSpan.getFirstNonEmptyChild(offset)
                if (result != null) return result
            }
        }
        val firstChild = children.firstOrNull()
        children.clear()
        if (firstChild != null) {
            firstChild.children.clear()
            if (offset != null)
                firstChild.textRange = TextRange(offset, offset + firstChild.text.length)
            children.add(firstChild)
        }
        return firstChild
    }

    /**
     * Update the paragraph of the children recursively
     *
     * @param newParagraph The new paragraph
     */
    fun updateChildrenParagraph(newParagraph: _root_ide_package_.com.rk.texteditor.model.RichParagraph) {
        children.fastForEach { childRichSpan ->
            childRichSpan.paragraph = newParagraph
            childRichSpan.updateChildrenParagraph(newParagraph)
        }
    }

    fun copy(): _root_ide_package_.com.rk.texteditor.model.RichParagraph {
        val newParagraph = _root_ide_package_.com.rk.texteditor.model.RichParagraph(
            paragraphStyle = paragraphStyle,
            type = type.copy(),
        )
        children.fastForEach { childRichSpan ->
            val newRichSpan = childRichSpan.copy(newParagraph)
            newRichSpan.paragraph = newParagraph
            newParagraph.children.add(newRichSpan)
        }
        return newParagraph
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(" - Start Text: ${type.startRichSpan}")
        stringBuilder.appendLine()
        children.fastForEachIndexed { index, richTextStyle ->
            getRichTextStyleTreeRepresentation(stringBuilder, index, richTextStyle, " -")
        }
        return stringBuilder.toString()
    }

    companion object {
        @OptIn(ExperimentalTextApi::class)
        val DefaultParagraphStyle = ParagraphStyle(
            textAlign = TextAlign.Left,
            lineBreak = LineBreak.Heading,
        )
    }
}