package com.rk.texteditor.utils

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.isUnspecified
import com.rk.texteditor.model.RichParagraph

@OptIn(ExperimentalTextApi::class)
internal fun ParagraphStyle.unmerge(
    other: ParagraphStyle?,
): ParagraphStyle {
    if (other == null) return this

    return ParagraphStyle(
        textAlign = if (other.textAlign != null) com.rk.texteditor.model.RichParagraph.DefaultParagraphStyle.textAlign else this.textAlign,
        textDirection = if (other.textDirection != null) com.rk.texteditor.model.RichParagraph.DefaultParagraphStyle.textDirection else this.textDirection,
        lineHeight = if (other.lineHeight.isSpecified) com.rk.texteditor.model.RichParagraph.DefaultParagraphStyle.lineHeight else this.lineHeight,
        textIndent = if (other.textIndent != null) com.rk.texteditor.model.RichParagraph.DefaultParagraphStyle.textIndent else this.textIndent,
        platformStyle = if (other.platformStyle != null) com.rk.texteditor.model.RichParagraph.DefaultParagraphStyle.platformStyle else this.platformStyle,
        lineHeightStyle = if (other.lineHeightStyle != null) com.rk.texteditor.model.RichParagraph.DefaultParagraphStyle.lineHeightStyle else this.lineHeightStyle,
        lineBreak = if (other.lineBreak != null) com.rk.texteditor.model.RichParagraph.DefaultParagraphStyle.lineBreak else this.lineBreak,
        hyphens = if (other.hyphens != null) com.rk.texteditor.model.RichParagraph.DefaultParagraphStyle.hyphens else this.hyphens,
    )
}

@OptIn(ExperimentalTextApi::class)
internal fun ParagraphStyle.isSpecifiedFieldsEquals(other: ParagraphStyle? = null): Boolean {
    if (other == null) return false

    if (other.textAlign != null && this.textAlign != other.textAlign) return false
    if (other.textDirection != null && this.textDirection != other.textDirection) return false
    if (!other.lineHeight.isUnspecified && this.lineHeight != other.lineHeight) return false
    if (other.textIndent != null && this.textIndent != other.textIndent) return false
    if (other.platformStyle != null && this.platformStyle != other.platformStyle) return false
    if (other.lineHeightStyle != null && this.lineHeightStyle != other.lineHeightStyle) return false
    if (other.lineBreak != null && this.lineBreak != other.lineBreak) return false
    if (other.hyphens != null && this.hyphens != other.hyphens) return false

    return true
}