package com.digitalgreen.farmerchat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digitalgreen.farmerchat.ui.theme.DesignSystem

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        val segments = parseMarkdownSegments(text)
        
        segments.forEach { segment ->
            when (segment.type) {
                MarkdownType.HEADING1 -> {
                    withStyle(style = SpanStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )) {
                        append(segment.text.removePrefix("# "))
                    }
                    append("\n\n")
                }
                MarkdownType.HEADING2 -> {
                    withStyle(style = SpanStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )) {
                        append(segment.text.removePrefix("## "))
                    }
                    append("\n\n")
                }
                MarkdownType.HEADING3 -> {
                    withStyle(style = SpanStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )) {
                        append(segment.text.removePrefix("### "))
                    }
                    append("\n\n")
                }
                MarkdownType.BOLD -> {
                    withStyle(style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )) {
                        append(segment.text.removeSurrounding("**"))
                    }
                }
                MarkdownType.ITALIC -> {
                    withStyle(style = SpanStyle(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )) {
                        append(segment.text.removeSurrounding("*"))
                    }
                }
                MarkdownType.CODE_INLINE -> {
                    withStyle(style = SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = MaterialTheme.colorScheme.surfaceVariant,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )) {
                        append(" ${segment.text.removeSurrounding("`")} ")
                    }
                }
                MarkdownType.BULLET_POINT -> {
                    append("• ")
                    append(segment.text.removePrefix("- "))
                    append("\n")
                }
                MarkdownType.NUMBER_POINT -> {
                    val number = segment.text.substringBefore(". ")
                    val content = segment.text.substringAfter(". ")
                    withStyle(style = SpanStyle(
                        fontWeight = FontWeight.Medium,
                        color = DesignSystem.Colors.Primary
                    )) {
                        append("$number. ")
                    }
                    append(content)
                    append("\n")
                }
                MarkdownType.PARAGRAPH -> {
                    append(segment.text)
                    append("\n\n")
                }
                MarkdownType.LINE_BREAK -> {
                    append("\n")
                }
                MarkdownType.NORMAL -> {
                    append(segment.text)
                }
            }
        }
    }
    
    Text(
        text = annotatedString,
        modifier = modifier,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )
}

@Composable
fun CodeBlock(
    code: String,
    language: String = "",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(DesignSystem.Spacing.md)
    ) {
        Text(
            text = code.trim(),
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )
    }
}

private fun parseMarkdownSegments(text: String): List<MarkdownSegment> {
    val segments = mutableListOf<MarkdownSegment>()
    val lines = text.split("\n")
    
    var i = 0
    while (i < lines.size) {
        val line = lines[i]
        
        when {
            line.startsWith("### ") -> {
                segments.add(MarkdownSegment(MarkdownType.HEADING3, line))
            }
            line.startsWith("## ") -> {
                segments.add(MarkdownSegment(MarkdownType.HEADING2, line))
            }
            line.startsWith("# ") -> {
                segments.add(MarkdownSegment(MarkdownType.HEADING1, line))
            }
            line.matches(Regex("^\\d+\\. .*")) -> {
                segments.add(MarkdownSegment(MarkdownType.NUMBER_POINT, line))
            }
            line.startsWith("- ") || line.startsWith("* ") -> {
                segments.add(MarkdownSegment(MarkdownType.BULLET_POINT, line))
            }
            line.isBlank() -> {
                if (segments.isNotEmpty() && segments.last().type != MarkdownType.LINE_BREAK) {
                    segments.add(MarkdownSegment(MarkdownType.LINE_BREAK, ""))
                }
            }
            else -> {
                // Process inline markdown within the line
                val processedLine = processInlineMarkdown(line)
                segments.addAll(processedLine)
                segments.add(MarkdownSegment(MarkdownType.PARAGRAPH, ""))
            }
        }
        i++
    }
    
    return segments.filter { it.text.isNotBlank() || it.type == MarkdownType.LINE_BREAK }
}

private fun processInlineMarkdown(text: String): List<MarkdownSegment> {
    val segments = mutableListOf<MarkdownSegment>()
    var remaining = text
    
    while (remaining.isNotEmpty()) {
        when {
            remaining.contains("**") -> {
                val start = remaining.indexOf("**")
                val end = remaining.indexOf("**", start + 2)
                if (end != -1) {
                    if (start > 0) {
                        segments.add(MarkdownSegment(MarkdownType.NORMAL, remaining.substring(0, start)))
                    }
                    segments.add(MarkdownSegment(MarkdownType.BOLD, remaining.substring(start, end + 2)))
                    remaining = remaining.substring(end + 2)
                } else {
                    segments.add(MarkdownSegment(MarkdownType.NORMAL, remaining))
                    break
                }
            }
            remaining.contains("*") && !remaining.contains("**") -> {
                val start = remaining.indexOf("*")
                val end = remaining.indexOf("*", start + 1)
                if (end != -1) {
                    if (start > 0) {
                        segments.add(MarkdownSegment(MarkdownType.NORMAL, remaining.substring(0, start)))
                    }
                    segments.add(MarkdownSegment(MarkdownType.ITALIC, remaining.substring(start, end + 1)))
                    remaining = remaining.substring(end + 1)
                } else {
                    segments.add(MarkdownSegment(MarkdownType.NORMAL, remaining))
                    break
                }
            }
            remaining.contains("`") -> {
                val start = remaining.indexOf("`")
                val end = remaining.indexOf("`", start + 1)
                if (end != -1) {
                    if (start > 0) {
                        segments.add(MarkdownSegment(MarkdownType.NORMAL, remaining.substring(0, start)))
                    }
                    segments.add(MarkdownSegment(MarkdownType.CODE_INLINE, remaining.substring(start, end + 1)))
                    remaining = remaining.substring(end + 1)
                } else {
                    segments.add(MarkdownSegment(MarkdownType.NORMAL, remaining))
                    break
                }
            }
            else -> {
                segments.add(MarkdownSegment(MarkdownType.NORMAL, remaining))
                break
            }
        }
    }
    
    return segments
}

private data class MarkdownSegment(
    val type: MarkdownType,
    val text: String
)

private enum class MarkdownType {
    HEADING1,
    HEADING2,
    HEADING3,
    BOLD,
    ITALIC,
    CODE_INLINE,
    BULLET_POINT,
    NUMBER_POINT,
    PARAGRAPH,
    LINE_BREAK,
    NORMAL
}