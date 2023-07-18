package com.rk.texteditor.parser.html

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.TextFieldValue
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import com.rk.texteditor.model.RichTextPart
import com.rk.texteditor.model.RichTextStyle
import com.rk.texteditor.model.RichTextValue
import com.rk.texteditor.parser.RichTextParser

internal object RichTextHtmlParser : RichTextParser<String> {

    override fun encode(input: String): RichTextValue {
        val openedTags = mutableListOf<Pair<String, Map<String, String>>>()
        var text = ""
        val currentStyles: MutableList<RichTextStyle> = mutableListOf()
        val parts: MutableList<RichTextPart> = mutableListOf()

        val handler = KsoupHtmlHandler
            .Builder()
            .onText {
                val lastOpenedTag = openedTags.lastOrNull()?.first
                if (lastOpenedTag in skippedHtmlElements) return@onText

                val addedText = removeHtmlTextExtraSpaces(
                    input = it,
                    trimStart = text.lastOrNull() == ' ' || text.lastOrNull() == '\n',
                )
                text += addedText

                parts.add(
                    RichTextPart(
                        fromIndex = text.length - addedText.length,
                        toIndex = text.lastIndex,
                        styles = currentStyles.toSet()
                    )
                )
            }
            .onOpenTag { name, attributes, _ ->
                openedTags.add(name to attributes)

                val cssStyleMap = attributes["style"]?.let { CssEncoder.parseCssStyle(it) } ?: emptyMap()
                val cssSpanStyle = CssEncoder.parseCssStyleMapToSpanStyle(cssStyleMap)
                val richTextStyle = htmlElementsStyleEncodeMap[name]

                if (cssSpanStyle != SpanStyle() || richTextStyle != null) {
                    val tagRichTextStyle = object : RichTextStyle {
                        override fun applyStyle(spanStyle: SpanStyle): SpanStyle {
                            val tagSpanStyle = richTextStyle?.applyStyle(spanStyle) ?: spanStyle
                            return tagSpanStyle.merge(cssSpanStyle)
                        }
                    }

                    currentStyles.add(tagRichTextStyle)
                }

                if (
                    text.lastOrNull() != null &&
                    text.lastOrNull()?.toString() != "\n" &&
                    name in htmlBlockElements
                ) {
                    text += "\n"
                }

                when (name) {
                    "br" -> {
                        text += "\n"
                    }
                }
            }
            .onCloseTag { name, _ ->
                openedTags.removeLastOrNull()
                currentStyles.removeLastOrNull()

                if (name in htmlBlockElements && text.lastOrNull()?.toString() != "\n") {
                    text += "\n"
                }

//                when (name) {
//                    "br" -> {
//                        text += "\n"
//                    }
//                }
            }
            .build()

        val parser = KsoupHtmlParser(
            handler = handler
        )

        parser.write(input)
        parser.end()

        return RichTextValue(
            textFieldValue = TextFieldValue(text),
            currentStyles = currentStyles.toSet(),
            parts = parts
        )
    }

    override fun decode(richTextValue: RichTextValue): String {
        val text = richTextValue.textFieldValue.text
        val parts = richTextValue.parts.sortedBy { it.fromIndex }

        val builder = StringBuilder()

        builder.append("<p>")

        for (part in parts) {
            val isInlined = part.fromIndex > 0 && text[part.fromIndex - 1] != '\n' && text[part.fromIndex] != '\n'

            val partText = text
                .substring(part.fromIndex..part.toIndex)
                .removePrefix("\n")
                .removeSuffix("\n")
                .replace("\n", "<br>")
            val partStyles = part.styles.toMutableSet()

            val tagName = partStyles
                .firstOrNull { htmlElementsStyleDecodeMap.containsKey(it) }
                ?.let {
                    partStyles.remove(it)
                    htmlElementsStyleDecodeMap[it]
                }
                ?: if (isInlined) "span" else "p"

            val tagStyle =
                if (partStyles.isEmpty()) ""
                else {
                    val stylesToApply = partStyles
                        .fold(SpanStyle()) { acc, richTextStyle -> richTextStyle.applyStyle(acc) }

                    val cssStyleMap = CssDecoder.decodeSpanStyleToCssStyleMap(stylesToApply)
                    " style=\"${CssDecoder.decodeCssStyleMap(cssStyleMap)}\""
                }

//            if (!isInlined && tagName != "p") builder.append("<p>")
            // Skip span tag if it's empty to improve html output
            if (tagName == "span" && tagStyle.isEmpty())
                builder.append(partText)
            else
                builder.append("<$tagName$tagStyle>$partText</$tagName>")
//            if (!isInlined && tagName != "p") builder.append("</p>")
        }

        return builder.toString()
    }

    /**
     * Encodes HTML elements to [RichTextStyle].
     *
     * @see <a href="https://www.w3schools.com/html/html_formatting.asp">HTML formatting</a>
     */
    private val htmlElementsStyleEncodeMap = mapOf(
        "b" to RichTextStyle.Bold,
        "strong" to RichTextStyle.Bold,
        "i" to RichTextStyle.Italic,
        "em" to RichTextStyle.Italic,
        "u" to RichTextStyle.Underline,
        "ins" to RichTextStyle.Underline,
        "strike" to RichTextStyle.Strikethrough,
        "del" to RichTextStyle.Strikethrough,
        "sub" to RichTextStyle.Subscript,
        "sup" to RichTextStyle.Superscript,
        "mark" to RichTextStyle.Mark,
        "small" to RichTextStyle.Small,
        "h1" to RichTextStyle.H1,
        "h2" to RichTextStyle.H2,
        "h3" to RichTextStyle.H3,
        "h4" to RichTextStyle.H4,
        "h5" to RichTextStyle.H5,
        "h6" to RichTextStyle.H6,
    )

    /**
     * Encodes HTML elements to [RichTextStyle].
     *
     * @see <a href="https://www.w3schools.com/html/html_formatting.asp">HTML formatting</a>
     */
    private val htmlElementsStyleDecodeMap = mapOf(
        RichTextStyle.Bold to "b",
        RichTextStyle.Italic to "i",
        RichTextStyle.Underline to "u",
        RichTextStyle.Strikethrough to "strike",
        RichTextStyle.Subscript to "sub",
        RichTextStyle.Superscript to "sup",
        RichTextStyle.Mark to "mark",
        RichTextStyle.Small to "small",
        RichTextStyle.H1 to "h1",
        RichTextStyle.H2 to "h2",
        RichTextStyle.H3 to "h3",
        RichTextStyle.H4 to "h4",
        RichTextStyle.H5 to "h5",
        RichTextStyle.H6 to "h6",
    )

}