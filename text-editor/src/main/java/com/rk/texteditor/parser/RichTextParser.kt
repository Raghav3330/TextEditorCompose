package com.rk.texteditor.parser

import com.rk.texteditor.model.RichTextValue

internal interface RichTextParser<T> {

    fun encode(input: T): RichTextValue

    fun decode(richTextValue: RichTextValue): T

}