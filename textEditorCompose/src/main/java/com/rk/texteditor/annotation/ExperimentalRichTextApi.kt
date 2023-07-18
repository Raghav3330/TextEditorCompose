package com.rk.texteditor.annotation

@RequiresOptIn(
    "This Rich Text API is experimental and is likely to change or to be removed in" +
            " the future.",
    level = RequiresOptIn.Level.WARNING
)
annotation class ExperimentalRichTextApi()
