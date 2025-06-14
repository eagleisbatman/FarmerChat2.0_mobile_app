package com.digitalgreen.farmerchat.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: Int = 15,
    lineHeight: Int = 22
) {
    val formattedText = parseMarkdown(text, color, fontSize)
    
    SelectionContainer {
        Text(
            text = formattedText,
            modifier = modifier,
            lineHeight = lineHeight.sp
        )
    }
}

private fun parseMarkdown(text: String, textColor: Color, baseFontSize: Int): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        val lines = text.split('\n')
        
        lines.forEachIndexed { lineIndex, line ->
            if (lineIndex > 0) append('\n')
            
            when {
                // Headers
                line.startsWith("### ") -> {
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = (baseFontSize + 2).sp,
                        color = textColor
                    )) {
                        append(line.substring(4))
                    }
                }
                line.startsWith("## ") -> {
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = (baseFontSize + 4).sp,
                        color = textColor
                    )) {
                        append(line.substring(3))
                    }
                }
                line.startsWith("# ") -> {
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = (baseFontSize + 6).sp,
                        color = textColor
                    )) {
                        append(line.substring(2))
                    }
                }
                // Bullet points
                line.trim().startsWith("- ") || line.trim().startsWith("* ") || line.trim().startsWith("• ") -> {
                    val bulletText = when {
                        line.trim().startsWith("• ") -> line.trim().substring(2)
                        else -> line.trim().substring(2)
                    }
                    append("  • ")
                    processInlineMarkdown(bulletText, textColor, baseFontSize)
                }
                // Numbered lists
                line.trim().matches(Regex("^\\d+\\.\\s.*")) -> {
                    val parts = line.trim().split(Regex("^\\d+\\.\\s"), 2)
                    val number = line.trim().substringBefore(".")
                    append("  $number. ")
                    if (parts.size > 1) {
                        processInlineMarkdown(parts[1], textColor, baseFontSize)
                    }
                }
                // Regular text
                else -> {
                    processInlineMarkdown(line, textColor, baseFontSize)
                }
            }
        }
    }
}

private fun AnnotatedString.Builder.processInlineMarkdown(
    text: String,
    textColor: Color,
    baseFontSize: Int
) {
    // Define regex patterns for different markdown elements
    val patterns = listOf(
        Triple("bold", Regex("\\*\\*(.+?)\\*\\*"), 1),
        Triple("underline", Regex("__(.*?)__"), 1),
        Triple("italic", Regex("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)"), 1),
        Triple("code", Regex("`(.+?)`"), 1),
        Triple("separator", Regex("^---$"), 0)
    )
    
    var remainingText = text
    var currentPosition = 0
    
    while (remainingText.isNotEmpty()) {
        var earliestMatch: MatchResult? = null
        var matchType = ""
        
        // Find the earliest match among all patterns
        patterns.forEach { (type, regex, _) ->
            val match = regex.find(remainingText)
            if (match != null && (earliestMatch == null || match.range.first < earliestMatch!!.range.first)) {
                earliestMatch = match
                matchType = type
            }
        }
        
        if (earliestMatch != null) {
            val match = earliestMatch!!
            
            // Append text before the match
            if (match.range.first > 0) {
                withStyle(SpanStyle(
                    color = textColor,
                    fontSize = baseFontSize.sp
                )) {
                    append(remainingText.substring(0, match.range.first))
                }
            }
            
            // Apply formatting based on match type
            when (matchType) {
                "bold" -> {
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = baseFontSize.sp
                    )) {
                        append(match.groupValues[1])
                    }
                }
                "underline" -> {
                    withStyle(SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        color = textColor,
                        fontSize = baseFontSize.sp
                    )) {
                        append(match.groupValues[1])
                    }
                }
                "italic" -> {
                    withStyle(SpanStyle(
                        fontStyle = FontStyle.Italic,
                        color = textColor,
                        fontSize = baseFontSize.sp
                    )) {
                        append(match.groupValues[1])
                    }
                }
                "code" -> {
                    withStyle(SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = (baseFontSize - 1).sp,
                        background = Color.Gray.copy(alpha = 0.2f),
                        color = textColor
                    )) {
                        append(match.groupValues[1])
                    }
                }
                "separator" -> {
                    append("───────────")
                }
            }
            
            // Move to the text after the match
            remainingText = remainingText.substring(match.range.last + 1)
        } else {
            // No more matches, append the remaining text
            withStyle(SpanStyle(
                color = textColor,
                fontSize = baseFontSize.sp
            )) {
                append(remainingText)
            }
            break
        }
    }
}