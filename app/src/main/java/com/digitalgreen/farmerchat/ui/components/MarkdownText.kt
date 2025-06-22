package com.digitalgreen.farmerchat.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.m3.Markdown

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: Int = 15,
    lineHeight: Int = 22,
    isStreaming: Boolean = false
) {
    val contentColor = if (color != Color.Unspecified) color else LocalContentColor.current
    
    // Always use safe markdown parsing - this handles both streaming and completed text
    // During streaming, it will gracefully handle incomplete markdown
    // After completion, it will render full markdown properly
    val annotatedText = remember(text) {
        parseStreamingSafeMarkdown(text, contentColor, fontSize)
    }
    
    Text(
        text = annotatedText,
        modifier = modifier.fillMaxWidth(),
        fontSize = fontSize.sp,
        lineHeight = lineHeight.sp
    )
}

// Safe markdown parser that handles incomplete syntax gracefully
private fun parseStreamingSafeMarkdown(
    text: String,
    textColor: Color,
    baseFontSize: Int
): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split('\n')
        
        lines.forEachIndexed { index, line ->
            if (index > 0) append('\n')
            
            when {
                // Headers - only process if they have content after the #
                line.trim().startsWith("### ") && line.trim().length > 4 -> {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = (baseFontSize + 2).sp,
                            color = textColor
                        )
                    ) {
                        parseSafeInlineMarkdown(line.trim().substring(4), this, textColor, baseFontSize + 2)
                    }
                }
                line.trim().startsWith("## ") && line.trim().length > 3 -> {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = (baseFontSize + 4).sp,
                            color = textColor
                        )
                    ) {
                        parseSafeInlineMarkdown(line.trim().substring(3), this, textColor, baseFontSize + 4)
                    }
                }
                line.trim().startsWith("# ") && line.trim().length > 2 -> {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = (baseFontSize + 6).sp,
                            color = textColor
                        )
                    ) {
                        parseSafeInlineMarkdown(line.trim().substring(2), this, textColor, baseFontSize + 6)
                    }
                }
                // Bullet points
                (line.trim().startsWith("- ") || line.trim().startsWith("* ")) && line.trim().length > 2 -> {
                    append("  • ")
                    parseSafeInlineMarkdown(line.trim().substring(2), this, textColor, baseFontSize)
                }
                // Numbered lists
                line.trim().matches(Regex("^\\d+\\.\\s.*")) -> {
                    val parts = line.trim().split(Regex("\\s"), 2)
                    append("  ${parts[0]} ")
                    if (parts.size > 1) {
                        parseSafeInlineMarkdown(parts[1], this, textColor, baseFontSize)
                    }
                }
                // Regular text with inline formatting
                else -> {
                    parseSafeInlineMarkdown(line, this, textColor, baseFontSize)
                }
            }
        }
    }
}

// Safe inline markdown parser that gracefully handles incomplete syntax
private fun parseSafeInlineMarkdown(
    text: String,
    builder: AnnotatedString.Builder,
    textColor: Color,
    fontSize: Int
) {
    var i = 0
    while (i < text.length) {
        when {
            // Bold (**text**) - only process if we have a complete pair
            i <= text.length - 2 && text.substring(i, i + 2) == "**" -> {
                val endIndex = text.indexOf("**", i + 2)
                if (endIndex != -1 && endIndex > i + 2) {
                    // Found complete bold syntax
                    val boldContent = text.substring(i + 2, endIndex)
                    builder.withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontSize = fontSize.sp
                        )
                    ) {
                        // Recursively parse content inside bold
                        parseSafeInlineMarkdown(boldContent, this, textColor, fontSize)
                    }
                    i = endIndex + 2
                } else {
                    // Incomplete bold syntax, treat as regular text
                    builder.append(text[i])
                    i++
                }
            }
            // Italic (*text*) - only process if we have a complete pair and not part of **
            text[i] == '*' && 
            (i == 0 || text[i - 1] != '*') && 
            (i == text.length - 1 || text[i + 1] != '*') -> {
                val endIndex = findSafeSingleAsterisk(text, i + 1)
                if (endIndex != -1 && endIndex > i + 1) {
                    // Found complete italic syntax
                    val italicContent = text.substring(i + 1, endIndex)
                    builder.withStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic,
                            color = textColor,
                            fontSize = fontSize.sp
                        )
                    ) {
                        // Recursively parse content inside italic
                        parseSafeInlineMarkdown(italicContent, this, textColor, fontSize)
                    }
                    i = endIndex + 1
                } else {
                    // Incomplete italic syntax, treat as regular text
                    builder.append(text[i])
                    i++
                }
            }
            // Code (`text`) - only process if we have a complete pair
            text[i] == '`' -> {
                val endIndex = text.indexOf('`', i + 1)
                if (endIndex != -1 && endIndex > i + 1) {
                    // Found complete code syntax
                    builder.withStyle(
                        SpanStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = (fontSize - 1).sp,
                            background = Color.Gray.copy(alpha = 0.2f),
                            color = textColor
                        )
                    ) {
                        builder.append(" ${text.substring(i + 1, endIndex)} ")
                    }
                    i = endIndex + 1
                } else {
                    // Incomplete code syntax, treat as regular text
                    builder.append(text[i])
                    i++
                }
            }
            else -> {
                builder.append(text[i])
                i++
            }
        }
    }
}

// Helper to find single asterisk safely (not part of **)
private fun findSafeSingleAsterisk(text: String, startIndex: Int): Int {
    var i = startIndex
    while (i < text.length) {
        if (text[i] == '*' && 
            (i == text.length - 1 || text[i + 1] != '*') &&
            (i == 0 || text[i - 1] != '*')) {
            return i
        }
        i++
    }
    return -1
}