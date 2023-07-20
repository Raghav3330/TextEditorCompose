package com.rk.texteditor.parser

import com.rk.texteditor.model.RichTextState

internal interface RichTextStateParser<T> {

    fun encode(input: T): RichTextState

    fun decode(richTextState: RichTextState): T

}